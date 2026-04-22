package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.request.BallotEntryDTO;
import com.dhbw.eurovision.dto.request.BallotRequestDTO;
import com.dhbw.eurovision.dto.response.BallotResponseDTO;
import com.dhbw.eurovision.dto.response.VoteLogResponseDTO;
import com.dhbw.eurovision.entity.*;
import com.dhbw.eurovision.factory.VoteLogFactory;
import com.dhbw.eurovision.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for VoteLog — Eurovision ballot submission.
 *
 * ── JURY ballot ───────────────────────────────────────────────────────────────
 *   - Exactly 10 entries using points {12,10,8,7,6,5,4,3,2,1} each exactly once.
 *   - Grand Final only.
 *   - No own-country song.
 *   - One ballot per jury member per show.
 *
 * ── CITIZEN ballot ────────────────────────────────────────────────────────────
 *   - 1 to N entries (citizen decides which songs to vote for).
 *   - Raw point values stored directly (not fixed scale) — each value ≥ 1.
 *   - Total points across all entries ≤ 20 (Eurovision televote budget).
 *   - No own-country song.
 *   - One ballot per citizen per show.
 *
 * ── SHOW ELIGIBILITY (both voter types) ──────────────────────────────────────
 *   - Grand Final:   everyone can vote.
 *   - Semi-Final 1:  countries with song in SF1 + IT, ES, CH (Big 5/host assigned to SF1).
 *   - Semi-Final 2:  countries with song in SF2 + FR, DE, GB (Big 5 assigned to SF2).
 *   Per Eurovision 2025 rules, Big 5 + host do not compete in semi-finals but
 *   each is assigned to cast votes in one of them.
 */
@Service
public class VoteLogService {

    private static final String GRAND_FINAL     = "grand final";
    private static final int    CITIZEN_MAX_PTS = 20;

    /**
     * Big 5 + host semi-final voting assignments for Eurovision 2025.
     * Key = country code, Value = show name they vote in.
     */
    private static final Map<String, String> BIG5_SEMI_ASSIGNMENT = Map.of(
            "IT", "Semi-Final 1",
            "ES", "Semi-Final 1",
            "CH", "Semi-Final 1",
            "FR", "Semi-Final 2",
            "DE", "Semi-Final 2",
            "GB", "Semi-Final 2"
    );

    private final VoteLogRepository  voteLogRepository;
    private final SongRepository     songRepository;
    private final ShowRepository     showRepository;
    private final JuryRepository     juryRepository;
    private final CitizenRepository  citizenRepository;
    private final VoteLogFactory     voteLogFactory;

    public VoteLogService(VoteLogRepository voteLogRepository,
                          SongRepository songRepository,
                          ShowRepository showRepository,
                          JuryRepository juryRepository,
                          CitizenRepository citizenRepository,
                          VoteLogFactory voteLogFactory) {
        this.voteLogRepository = voteLogRepository;
        this.songRepository    = songRepository;
        this.showRepository    = showRepository;
        this.juryRepository    = juryRepository;
        this.citizenRepository = citizenRepository;
        this.voteLogFactory    = voteLogFactory;
    }

    public List<VoteLogResponseDTO> getAllVotes() {
        return voteLogRepository.findAll().stream()
                .map(voteLogFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BallotResponseDTO submitBallot(BallotRequestDTO dto) {

        // ── 1. Resolve show ──────────────────────────────────────────────
        Show show = showRepository.findById(dto.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found: " + dto.getShowId()));
        String showNameLower = show.getShowName() == null ? ""
                : show.getShowName().trim().toLowerCase();
        boolean isGrandFinal = showNameLower.equals(GRAND_FINAL);

        // ── 2. Resolve voter ─────────────────────────────────────────────
        if (dto.getJuryId() != null && dto.getCitizenId() != null) {
            throw new IllegalArgumentException(
                    "A ballot must come from either a Jury member or a Citizen, not both.");
        }

        Jury    jury    = null;
        Citizen citizen = null;
        String  voterCountry;
        boolean isJury;

        if (dto.getJuryId() != null) {
            isJury = true;
            jury   = juryRepository.findById(dto.getJuryId())
                    .orElseThrow(() -> new RuntimeException("Jury not found: " + dto.getJuryId()));
            voterCountry = jury.getCountry().getCountryCode();

            // Jury votes Grand Final only
            if (!isGrandFinal) {
                throw new IllegalArgumentException(
                        "Jury members can only vote in the Grand Final.");
            }
            // One ballot per show
            if (voteLogRepository.existsByShow_ShowIdAndJury_UserId(
                    show.getShowId(), jury.getUserId())) {
                throw new IllegalArgumentException(
                        "Jury member #" + jury.getUserId() +
                                " has already voted in: " + show.getShowName());
            }

        } else if (dto.getCitizenId() != null) {
            isJury   = false;
            citizen  = citizenRepository.findById(dto.getCitizenId())
                    .orElseThrow(() -> new RuntimeException("Citizen not found: " + dto.getCitizenId()));
            voterCountry = citizen.getCountry().getCountryCode();

            // One ballot per show
            if (voteLogRepository.existsByShow_ShowIdAndCitizen_UserId(
                    show.getShowId(), citizen.getUserId())) {
                throw new IllegalArgumentException(
                        "Citizen #" + citizen.getUserId() +
                                " has already voted in: " + show.getShowName());
            }

        } else {
            throw new IllegalArgumentException("A ballot must have a voter (juryId or citizenId).");
        }

        // ── 3. Show eligibility ──────────────────────────────────────────
        if (!isGrandFinal) {
            boolean eligible = isCountryEligibleForSemiFinal(voterCountry, show);
            if (!eligible) {
                String assigned = BIG5_SEMI_ASSIGNMENT.get(voterCountry);
                String hint = (assigned != null)
                        ? " Your country is assigned to vote in " + assigned + "."
                        : " Your country competes in a different semi-final.";
                throw new IllegalArgumentException(
                        "Country " + voterCountry +
                                " is not eligible to vote in " + show.getShowName() + "." + hint);
            }
        }

        // ── 4. Validate entries ──────────────────────────────────────────
        List<BallotEntryDTO> entries = dto.getVotes();
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("A ballot must contain at least one entry.");
        }

        if (isJury) {
            validateJuryEntries(entries);
        } else {
            validateCitizenEntries(entries);
        }

        // ── 5. Validate each song ────────────────────────────────────────
        Set<Long> showSongIds = show.getSongs().stream()
                .map(Song::getSongId)
                .collect(Collectors.toSet());

        String sessionId = UUID.randomUUID().toString();
        List<VoteLog> rows = new ArrayList<>();

        for (BallotEntryDTO entry : entries) {
            Song song = songRepository.findById(entry.getSongId())
                    .orElseThrow(() -> new RuntimeException(
                            "Song not found: " + entry.getSongId()));

            if (!showSongIds.contains(song.getSongId())) {
                throw new IllegalArgumentException(
                        "Song '" + song.getSongTitle() + "' is not part of show: " +
                                show.getShowName());
            }
            if (song.getCountry().getCountryCode().equals(voterCountry)) {
                throw new IllegalArgumentException(
                        "You cannot vote for your own country's song (" +
                                song.getSongTitle() + " — " + voterCountry + ").");
            }

            rows.add(voteLogFactory.toEntity(
                    sessionId, show, song, entry.getPoints(), jury, citizen));
        }

        // ── 6. Save atomically ───────────────────────────────────────────
        List<VoteLog> saved = voteLogRepository.saveAll(rows);
        List<VoteLogResponseDTO> responseDTOs = saved.stream()
                .map(voteLogFactory::toResponseDTO)
                .collect(Collectors.toList());

        return new BallotResponseDTO(sessionId, show.getShowId(), responseDTOs);
    }

    // ── Private helpers ───────────────────────────────────────────────

    /**
     * A country is eligible for a semi-final if:
     *   a) it has a song competing in that show (regular participant), OR
     *   b) it is a Big 5 / host country assigned to vote in that specific semi-final.
     */
    private boolean isCountryEligibleForSemiFinal(String countryCode, Show show) {
        // Regular participant: country has a song in this show
        boolean competesInShow = show.getSongs().stream()
                .anyMatch(s -> s.getCountry().getCountryCode().equals(countryCode));
        if (competesInShow) return true;

        // Big 5 / host: check assignment map
        String assignedShow = BIG5_SEMI_ASSIGNMENT.get(countryCode);
        return assignedShow != null &&
                assignedShow.equalsIgnoreCase(show.getShowName().trim());
    }

    /**
     * Jury ballot: exactly 10 entries, points = {12,10,8,7,6,5,4,3,2,1} each once.
     */
    private void validateJuryEntries(List<BallotEntryDTO> entries) {
        if (entries.size() != 10) {
            throw new IllegalArgumentException(
                    "Jury ballot must contain exactly 10 entries. Got: " + entries.size());
        }
        Set<Integer> used = new HashSet<>();
        for (BallotEntryDTO e : entries) {
            if (!VoteLog.VALID_POINTS.contains(e.getPoints())) {
                throw new IllegalArgumentException(
                        "Invalid jury points: " + e.getPoints() +
                                ". Must be one of: 12,10,8,7,6,5,4,3,2,1");
            }
            if (!used.add(e.getPoints())) {
                throw new IllegalArgumentException(
                        "Duplicate points value in jury ballot: " + e.getPoints());
            }
        }
    }

    /**
     * Citizen ballot: 1–N entries, each ≥ 1 point, total ≤ 20.
     * Raw values stored directly — aggregation to scale done in ScoreService.
     */
    private void validateCitizenEntries(List<BallotEntryDTO> entries) {
        int total = 0;
        Set<Long> votedSongs = new HashSet<>();
        for (BallotEntryDTO e : entries) {
            if (e.getPoints() == null || e.getPoints() < 1) {
                throw new IllegalArgumentException(
                        "Each citizen vote must award at least 1 point.");
            }
            if (!votedSongs.add(e.getSongId())) {
                throw new IllegalArgumentException(
                        "Duplicate song in citizen ballot: songId " + e.getSongId());
            }
            total += e.getPoints();
        }
        if (total > CITIZEN_MAX_PTS) {
            throw new IllegalArgumentException(
                    "Citizen ballot total exceeds 20 points. Total: " + total);
        }
    }
}