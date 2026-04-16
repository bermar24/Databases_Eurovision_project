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
 * Service for VoteLog.
 *
 * Enforced rules:
 *   1. Exactly 10 entries per ballot using points {12,10,8,7,6,5,4,3,2,1} each once.
 *   2. No own-country song.
 *   3. All songs must belong to the selected show.
 *   4. Voter's country must be eligible for the show:
 *        Semi-Final 1/2 → only countries in that semi-final's lineup
 *        Grand Final    → everyone
 *   5. Voter has not already submitted a ballot for this show (one ballot per show).
 */
@Service
public class VoteLogService {

    private static final String GRAND_FINAL = "Grand Final";

    private final VoteLogRepository voteLogRepository;
    private final SongRepository songRepository;
    private final ShowRepository showRepository;
    private final JuryRepository juryRepository;
    private final CitizenRepository citizenRepository;
    private final VoteLogFactory voteLogFactory;

    public VoteLogService(VoteLogRepository voteLogRepository,
                          SongRepository songRepository,
                          ShowRepository showRepository,
                          JuryRepository juryRepository,
                          CitizenRepository citizenRepository,
                          VoteLogFactory voteLogFactory) {
        this.voteLogRepository = voteLogRepository;
        this.songRepository = songRepository;
        this.showRepository    = showRepository;
        this.juryRepository = juryRepository;
        this.citizenRepository = citizenRepository;
        this.voteLogFactory = voteLogFactory;
    }

    public List<VoteLogResponseDTO> getAllVotes() {
        return voteLogRepository.findAll().stream()
                .map(voteLogFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Submit a complete ballot of 10 point assignments.
     * All 10 VoteLog rows are saved atomically.
     */
    @Transactional
    public BallotResponseDTO submitBallot(BallotRequestDTO dto) {

        // ── 1. Resolve show ──────────────────────────────────────────────
        Show show = showRepository.findById(dto.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found: " + dto.getShowId()));

        // ── 2. Resolve voter — exactly one of jury / citizen ─────────────
        if (dto.getJuryId() != null && dto.getCitizenId() != null) {
            throw new IllegalArgumentException(
                    "A ballot must come from either a Jury member or a Citizen, not both.");
        }

        Jury    jury    = null;
        Citizen citizen = null;
        String  voterCountry;

        if (dto.getJuryId() != null) {
            jury = juryRepository.findById(dto.getJuryId())
                    .orElseThrow(() -> new RuntimeException("Jury not found: " + dto.getJuryId()));
            voterCountry = jury.getCountry().getCountryCode();

            // ── 3a. One ballot per show (jury) ───────────────────────────
            if (voteLogRepository.existsByShow_ShowIdAndJury_UserId(
                    show.getShowId(), jury.getUserId())) {
                throw new IllegalArgumentException(
                        "Jury member #" + jury.getUserId() +
                                " has already voted in show: " + show.getShowName());
            }

        } else if (dto.getCitizenId() != null) {
            citizen = citizenRepository.findById(dto.getCitizenId())
                    .orElseThrow(() -> new RuntimeException("Citizen not found: " + dto.getCitizenId()));
            voterCountry = citizen.getCountry().getCountryCode();

            // ── 3b. One ballot per show (citizen) ────────────────────────
            if (voteLogRepository.existsByShow_ShowIdAndCitizen_UserId(
                    show.getShowId(), citizen.getUserId())) {
                throw new IllegalArgumentException(
                        "Citizen #" + citizen.getUserId() +
                                " has already voted in show: " + show.getShowName());
            }

        } else {
            throw new IllegalArgumentException(
                    "A ballot must have a voter (juryId or citizenId).");
        }

        // ── 4. Show eligibility for voter's country ──────────────────────
        if (!GRAND_FINAL.equalsIgnoreCase(show.getShowName())) {
            // For semi-finals, voter's country must be in the show's song list
            boolean countryInShow = show.getSongs().stream()
                    .anyMatch(s -> s.getCountry().getCountryCode().equals(voterCountry));
            if (!countryInShow) {
                throw new IllegalArgumentException(
                        "Country " + voterCountry +
                                " is not participating in " + show.getShowName() +
                                ". You can only vote in the show your country competes in, or the Grand Final.");
            }
        }

        // ── 5. Validate ballot entries ───────────────────────────────────
        List<BallotEntryDTO> entries = dto.getVotes();
        if (entries == null || entries.size() != 10) {
            throw new IllegalArgumentException(
                    "A ballot must contain exactly 10 entries. Got: " +
                            (entries == null ? 0 : entries.size()));
        }

        // Check points set = {12,10,8,7,6,5,4,3,2,1}
        Set<Integer> usedPoints = new HashSet<>();
        for (BallotEntryDTO entry : entries) {
            if (!VoteLog.VALID_POINTS.contains(entry.getPoints())) {
                throw new IllegalArgumentException(
                        "Invalid points value: " + entry.getPoints() +
                                ". Must be one of: 12,10,8,7,6,5,4,3,2,1");
            }
            if (!usedPoints.add(entry.getPoints())) {
                throw new IllegalArgumentException(
                        "Duplicate points value: " + entry.getPoints() +
                                ". Each point value must be used exactly once.");
            }
        }
        if (usedPoints.size() != 10) {
            throw new IllegalArgumentException(
                    "Ballot must use all 10 point values exactly once.");
        }

        // Build song lookup for this show
        Set<Long> showSongIds = show.getSongs().stream()
                .map(Song::getSongId)
                .collect(Collectors.toSet());

        // ── 6. Validate each song entry ──────────────────────────────────
        String sessionId = UUID.randomUUID().toString();
        List<VoteLog> rows = new ArrayList<>();

        for (BallotEntryDTO entry : entries) {
            Song song = songRepository.findById(entry.getSongId())
                    .orElseThrow(() -> new RuntimeException(
                            "Song not found: " + entry.getSongId()));

            // Song must be in this show
            if (!showSongIds.contains(song.getSongId())) {
                throw new IllegalArgumentException(
                        "Song #" + song.getSongId() + " (" + song.getSingerName() +
                                ") is not part of show: " + show.getShowName());
            }

            // No own-country vote
            if (song.getCountry().getCountryCode().equals(voterCountry)) {
                throw new IllegalArgumentException(
                        "You cannot vote for your own country's song (" +
                                song.getSingerName() + " — " + voterCountry + ").");
            }

            rows.add(voteLogFactory.toEntity(
                    sessionId, show, song, entry.getPoints(), jury, citizen));
        }

        // ── 7. Save all 10 rows atomically ───────────────────────────────
        List<VoteLog> saved = voteLogRepository.saveAll(rows);

        List<VoteLogResponseDTO> responseDTOs = saved.stream()
                .map(voteLogFactory::toResponseDTO)
                .collect(Collectors.toList());

        return new BallotResponseDTO(sessionId, show.getShowId(), responseDTOs);
    }
}
