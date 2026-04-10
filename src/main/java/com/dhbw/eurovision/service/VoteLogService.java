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
 * Service for VoteLog — handles casting and querying of votes.
 * This is one of the core business operations of the Eurovision app.
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
     * Cast a vote. Exactly one of dto.juryId or dto.citizenId must be set.
     */
    public VoteLogResponseDTO castVote(VoteLogRequestDTO dto) {
        Song song = songRepository.findById(dto.getSongId())
                .orElseThrow(() -> new RuntimeException("Song not found: " + dto.getSongId()));

        Jury jury = null;
        Citizen citizen = null;

        if (dto.getJuryId() != null && dto.getCitizenId() != null) {
            throw new IllegalArgumentException("A vote must come from either a Jury or a Citizen, not both.");
        }
        if (dto.getJuryId() != null) {
            jury = juryRepository.findById(dto.getJuryId())
                    .orElseThrow(() -> new RuntimeException("Jury not found: " + dto.getJuryId()));
        } else if (dto.getCitizenId() != null) {
            citizen = citizenRepository.findById(dto.getCitizenId())
                    .orElseThrow(() -> new RuntimeException("Citizen not found: " + dto.getCitizenId()));
        } else {
            throw new IllegalArgumentException("A vote must have a voter (juryId or citizenId).");
        }

        VoteLog voteLog = voteLogFactory.toEntity(dto, song, jury, citizen);
        return voteLogFactory.toResponseDTO(voteLogRepository.save(voteLog));
    }

    // TODO: getVotesBySong(Long songId), getVotesByJury(Long juryId), etc.
    // TODO: implement score calculation trigger or scheduled job
}
