package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.request.VoteLogRequestDTO;
import com.dhbw.eurovision.dto.response.VoteLogResponseDTO;
import com.dhbw.eurovision.entity.*;
import org.springframework.stereotype.Component;

/**
 * Factory for VoteLog — converts between Entity <-> DTO.
 * Song, Jury, and Citizen must be resolved by VoteLogService before calling toEntity().
 */
@Component
public class VoteLogFactory {

    public VoteLog toEntity(String sessionId, Show show, Song song,
                            Integer points, Jury jury, Citizen citizen) {
        VoteLog vl = new VoteLog();
        vl.setVoteSessionId(sessionId);
        vl.setShow(show);
        vl.setSong(song);
        vl.setPoints(points);
        vl.setJury(jury);
        vl.setCitizen(citizen);
        return vl;
    }

    public VoteLogResponseDTO toResponseDTO(VoteLog vl) {
        return new VoteLogResponseDTO(
                vl.getVoteLogId(),
                vl.getSong()    != null ? vl.getSong().getSongId()       : null,
                vl.getShow()    != null ? vl.getShow().getShowId()       : null,
                vl.getJury()    != null ? vl.getJury().getUserId()       : null,
                vl.getCitizen() != null ? vl.getCitizen().getUserId()    : null,
                vl.getPoints(),
                vl.getVoteSessionId()
        );
    }
}