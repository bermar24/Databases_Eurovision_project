package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.request.VoteLogRequestDTO;
import com.dhbw.eurovision.dto.response.VoteLogResponseDTO;
import com.dhbw.eurovision.entity.Citizen;
import com.dhbw.eurovision.entity.Jury;
import com.dhbw.eurovision.entity.Song;
import com.dhbw.eurovision.entity.VoteLog;
import org.springframework.stereotype.Component;

/**
 * Factory for VoteLog — converts between Entity <-> DTO.
 * Song, Jury, and Citizen must be resolved by VoteLogService before calling toEntity().
 */
@Component
public class VoteLogFactory {

    public VoteLog toEntity(VoteLogRequestDTO dto, Song song, Jury jury, Citizen citizen) {
        VoteLog voteLog = new VoteLog();
        voteLog.setSong(song);
        voteLog.setJury(jury);
        voteLog.setCitizen(citizen);
        voteLog.setPoints(dto.getPoints());
        return voteLog;
    }

    public VoteLogResponseDTO toResponseDTO(VoteLog voteLog) {
        return new VoteLogResponseDTO(
                voteLog.getVoteLogId(),
                voteLog.getSong() != null ? voteLog.getSong().getSongId() : null,
                voteLog.getJury() != null ? voteLog.getJury().getUserId() : null,
                voteLog.getCitizen() != null ? voteLog.getCitizen().getUserId() : null,
                voteLog.getPoints()
        );
    }
}