package com.dhbw.eurovision.config;

import com.dhbw.eurovision.entity.Admin;
import com.dhbw.eurovision.entity.Citizen;
import com.dhbw.eurovision.entity.Country;
import com.dhbw.eurovision.entity.Jury;
import com.dhbw.eurovision.entity.Show;
import com.dhbw.eurovision.entity.Song;
import com.dhbw.eurovision.repository.AdminRepository;
import com.dhbw.eurovision.repository.CitizenRepository;
import com.dhbw.eurovision.repository.CountryRepository;
import com.dhbw.eurovision.repository.JuryRepository;
import com.dhbw.eurovision.repository.ShowRepository;
import com.dhbw.eurovision.repository.SongRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads seed data from {@code seed-data.json} (on the classpath) into the
 * database on first startup. The seeder is a no-op when the country table
 * already contains rows, making it safe to run on every restart.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String SEED_FILE = "seed-data.json";

    private final CountryRepository countryRepository;
    private final ShowRepository    showRepository;
    private final SongRepository    songRepository;
    private final AdminRepository   adminRepository;
    private final JuryRepository    juryRepository;
    private final CitizenRepository citizenRepository;
    private final ObjectMapper      objectMapper;

    public DataSeeder(CountryRepository countryRepository,
                      ShowRepository showRepository,
                      SongRepository songRepository,
                      AdminRepository adminRepository,
                      JuryRepository juryRepository,
                      CitizenRepository citizenRepository,
                      ObjectMapper objectMapper) {
        this.countryRepository = countryRepository;
        this.showRepository    = showRepository;
        this.songRepository    = songRepository;
        this.adminRepository   = adminRepository;
        this.juryRepository    = juryRepository;
        this.citizenRepository = citizenRepository;
        this.objectMapper      = objectMapper;
    }

    @Override
    public void run(String... args) {
        if (countryRepository.count() > 0) {
            log.warn("DataSeeder: database already contains data — skipping seed.");
            return;
        }

        log.info("DataSeeder: database is empty, loading seed data from {}...", SEED_FILE);

        try {
            ClassPathResource resource = new ClassPathResource(SEED_FILE);
            if (!resource.exists()) {
                log.warn("DataSeeder: {} not found on classpath — skipping seed.", SEED_FILE);
                return;
            }

            JsonNode root;
            try (InputStream is = resource.getInputStream()) {
                root = objectMapper.readTree(is);
            }

            // 1. Countries
            Map<String, Country> countryMap = seedCountries(root.path("countries"));

            // 2. Shows
            Map<String, Show> showMap = seedShows(root.path("shows"));

            // 3. Songs (linked to countries)
            Map<String, Song> songByCountryCode = seedSongs(root.path("songs"), countryMap);

            // 4. Show ↔ Song assignments
            seedShowAssignments(root.path("show_assignments"), showMap, songByCountryCode);

            // 5. Admins
            seedAdmins(root.path("admins"), countryMap);

            // 6. Jury members
            seedJury(root.path("jury"), countryMap);

            // 7. Citizens
            seedCitizens(root.path("citizens"), countryMap);

            log.info("DataSeeder: seed completed successfully.");

        } catch (Exception e) {
            log.error("DataSeeder: failed to load seed data — {}", e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Map<String, Country> seedCountries(JsonNode node) {
        Map<String, Country> map = new HashMap<>();
        if (node.isMissingNode() || !node.isArray()) return map;

        List<Country> toSave = new ArrayList<>();
        for (JsonNode item : node) {
            String code = item.path("countryCode").asText(null);
            String name = item.path("countryName").asText(null);
            if (code == null || name == null) continue;

            Country country = new Country();
            country.setCountryCode(code);
            country.setCountryName(name);
            toSave.add(country);
        }

        List<Country> saved = countryRepository.saveAll(toSave);
        saved.forEach(c -> map.put(c.getCountryCode(), c));
        log.info("DataSeeder: saved {} countries.", saved.size());
        return map;
    }

    private Map<String, Show> seedShows(JsonNode node) {
        Map<String, Show> map = new HashMap<>();
        if (node.isMissingNode() || !node.isArray()) return map;

        List<Show> toSave = new ArrayList<>();
        for (JsonNode item : node) {
            String name = item.path("showName").asText(null);
            if (name == null) continue;

            Show show = new Show();
            show.setShowName(name);
            toSave.add(show);
        }

        List<Show> saved = showRepository.saveAll(toSave);
        saved.forEach(s -> map.put(s.getShowName(), s));
        log.info("DataSeeder: saved {} shows.", saved.size());
        return map;
    }

    /**
     * Seeds songs and returns a map of countryCode → Song so that
     * show assignments can look up songs by their country code.
     */
    private Map<String, Song> seedSongs(JsonNode node, Map<String, Country> countryMap) {
        Map<String, Song> map = new HashMap<>();
        if (node.isMissingNode() || !node.isArray()) return map;

        List<Song> toSave = new ArrayList<>();
        for (JsonNode item : node) {
            String singerName = item.path("singerName").asText(null);
            String countryCode = item.path("countryCode").asText(null);
            if (singerName == null || countryCode == null) continue;

            Country country = countryMap.get(countryCode);
            if (country == null) {
                log.warn("DataSeeder: unknown countryCode '{}' for song '{}' — skipping.", countryCode, singerName);
                continue;
            }

            Song song = new Song();
            song.setSingerName(singerName);
            song.setCountry(country);
            toSave.add(song);
        }

        List<Song> saved = songRepository.saveAll(toSave);
        // Index by country code for show-assignment lookup (one song per country)
        saved.forEach(s -> {
            if (s.getCountry() != null) {
                map.put(s.getCountry().getCountryCode(), s);
            }
        });
        log.info("DataSeeder: saved {} songs.", saved.size());
        return map;
    }

    /**
     * Assigns songs to shows based on the {@code show_assignments} map in the
     * seed file. Each entry maps a show name to a list of country codes.
     */
    private void seedShowAssignments(JsonNode node,
                                     Map<String, Show> showMap,
                                     Map<String, Song> songByCountryCode) {
        if (node.isMissingNode() || !node.isObject()) return;

        node.fields().forEachRemaining(entry -> {
            String showName = entry.getKey();
            Show show = showMap.get(showName);
            if (show == null) {
                log.warn("DataSeeder: show '{}' not found — skipping assignments.", showName);
                return;
            }

            List<Song> songsForShow = new ArrayList<>();
            for (JsonNode codeNode : entry.getValue()) {
                String code = codeNode.asText(null);
                Song song = songByCountryCode.get(code);
                if (song == null) {
                    log.warn("DataSeeder: no song for country '{}' in show '{}' — skipping.", code, showName);
                } else {
                    songsForShow.add(song);
                }
            }

            show.getSongs().addAll(songsForShow);
            showRepository.save(show);
            log.info("DataSeeder: assigned {} songs to show '{}'.", songsForShow.size(), showName);
        });
    }

    private void seedAdmins(JsonNode node, Map<String, Country> countryMap) {
        if (node.isMissingNode() || !node.isArray()) return;

        List<Admin> toSave = new ArrayList<>();
        for (JsonNode item : node) {
            String countryCode = item.path("countryCode").asText(null);
            int adminLevel = item.path("adminLevel").asInt(1);
            if (countryCode == null) continue;

            Country country = countryMap.get(countryCode);
            if (country == null) {
                log.warn("DataSeeder: unknown countryCode '{}' for admin — skipping.", countryCode);
                continue;
            }

            Admin admin = new Admin();
            admin.setCountry(country);
            admin.setAdminLevel(adminLevel);
            toSave.add(admin);
        }

        List<Admin> saved = adminRepository.saveAll(toSave);
        log.info("DataSeeder: saved {} admins.", saved.size());
    }

    private void seedJury(JsonNode node, Map<String, Country> countryMap) {
        if (node.isMissingNode() || !node.isArray()) return;

        List<Jury> toSave = new ArrayList<>();
        for (JsonNode item : node) {
            String countryCode = item.path("countryCode").asText(null);
            String professionalBg = item.path("professionalBg").asText(null);
            if (countryCode == null) continue;

            Country country = countryMap.get(countryCode);
            if (country == null) {
                log.warn("DataSeeder: unknown countryCode '{}' for jury member — skipping.", countryCode);
                continue;
            }

            Jury jury = new Jury();
            jury.setCountry(country);
            jury.setProfessionalBg(professionalBg);
            toSave.add(jury);
        }

        List<Jury> saved = juryRepository.saveAll(toSave);
        log.info("DataSeeder: saved {} jury members.", saved.size());
    }

    private void seedCitizens(JsonNode node, Map<String, Country> countryMap) {
        if (node.isMissingNode() || !node.isArray()) return;

        List<Citizen> toSave = new ArrayList<>();
        for (JsonNode item : node) {
            String countryCode = item.path("countryCode").asText(null);
            String phoneNumber = item.path("phoneNumber").asText(null);
            if (countryCode == null || phoneNumber == null) continue;

            // Skip duplicates — phone_number has a UNIQUE constraint
            if (citizenRepository.existsByPhoneNumber(phoneNumber)) {
                log.warn("DataSeeder: citizen with phone '{}' already exists — skipping.", phoneNumber);
                continue;
            }

            Country country = countryMap.get(countryCode);
            if (country == null) {
                log.warn("DataSeeder: unknown countryCode '{}' for citizen '{}' — skipping.", countryCode, phoneNumber);
                continue;
            }

            Citizen citizen = new Citizen();
            citizen.setCountry(country);
            citizen.setPhoneNumber(phoneNumber);
            toSave.add(citizen);
        }

        List<Citizen> saved = citizenRepository.saveAll(toSave);
        log.info("DataSeeder: saved {} citizens.", saved.size());
    }
}
