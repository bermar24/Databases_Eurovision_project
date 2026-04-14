package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.request.VoteLogRequestDTO;
import com.dhbw.eurovision.dto.response.VoteLogResponseDTO;
import com.dhbw.eurovision.entity.*;
import com.dhbw.eurovision.factory.VoteLogFactory;
import com.dhbw.eurovision.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for VoteLog.
 *
 * Enforced business rules:
 *   1. points must be one of: 12, 10, 8, 7, 6, 5, 4, 3, 2, 1
 *   2. voter's country must differ from the song's country (no own-country vote)
 *   3. exactly one of juryId / citizenId must be set
 */
@Service
public class VoteLogService {

    private final VoteLogRepository voteLogRepository;
    private final SongRepository songRepository;
    private final JuryRepository juryRepository;
    private final CitizenRepository citizenRepository;
    private final VoteLogFactory voteLogFactory;

    public VoteLogService(VoteLogRepository voteLogRepository,
                          SongRepository songRepository,
                          JuryRepository juryRepository,
                          CitizenRepository citizenRepository,
                          VoteLogFactory voteLogFactory) {
        this.voteLogRepository = voteLogRepository;
        this.songRepository = songRepository;
        this.juryRepository = juryRepository;
        this.citizenRepository = citizenRepository;
        this.voteLogFactory = voteLogFactory;
    }

    public List<VoteLogResponseDTO> getAllVotes() {
        return voteLogRepository.findAll()
                .stream()
                .map(voteLogFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cast a vote with full Eurovision validation.
     */
    public VoteLogResponseDTO castVote(VoteLogRequestDTO dto) {

        // ── 1. Validate points ───────────────────────────────────────────
        if (dto.getPoints() == null || !VoteLog.VALID_POINTS.contains(dto.getPoints())) {
            throw new IllegalArgumentException(
                    "Invalid points value: " + dto.getPoints() +
                            ". Must be one of: 12, 10, 8, 7, 6, 5, 4, 3, 2, 1"
            );
        }

        // ── 2. Resolve song ──────────────────────────────────────────────
        Song song = songRepository.findById(dto.getSongId())
                .orElseThrow(() -> new RuntimeException("Song not found: " + dto.getSongId()));

        String songCountry = song.getCountry().getCountryCode();

        // ── 3. Resolve voter — exactly one of jury / citizen ─────────────
        if (dto.getJuryId() != null && dto.getCitizenId() != null) {
            throw new IllegalArgumentException(
                    "A vote must come from either a Jury member or a Citizen, not both.");
        }

        Jury    jury    = null;
        Citizen citizen = null;

        if (dto.getJuryId() != null) {
            jury = juryRepository.findById(dto.getJuryId())
                    .orElseThrow(() -> new RuntimeException("Jury not found: " + dto.getJuryId()));

            // ── 4a. No-own-country rule for Jury ─────────────────────────
            String juryCountry = jury.getCountry().getCountryCode();
            if (juryCountry.equals(songCountry)) {
                throw new IllegalArgumentException(
                        "Jury member from " + juryCountry +
                                " cannot vote for their own country's song.");
            }

        } else if (dto.getCitizenId() != null) {
            citizen = citizenRepository.findById(dto.getCitizenId())
                    .orElseThrow(() -> new RuntimeException("Citizen not found: " + dto.getCitizenId()));

            // ── 4b. No-own-country rule for Citizen ──────────────────────
            String citizenCountry = citizen.getCountry().getCountryCode();
            if (citizenCountry.equals(songCountry)) {
                throw new IllegalArgumentException(
                        "Citizen from " + citizenCountry +
                                " cannot vote for their own country's song.");
            }

        } else {
            throw new IllegalArgumentException(
                    "A vote must have a voter (juryId or citizenId).");
        }

        // ── 5. Persist ───────────────────────────────────────────────────
        VoteLog voteLog = voteLogFactory.toEntity(dto, song, jury, citizen);
        return voteLogFactory.toResponseDTO(voteLogRepository.save(voteLog));
    }
}