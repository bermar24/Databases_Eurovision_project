package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.response.ScoreResponseDTO;
import com.dhbw.eurovision.entity.Score;
import org.springframework.stereotype.Component;

/**
 * Factory for Score — converts Entity -> ResponseDTO.
 * Score has no RequestDTO because it is always calculated, never directly created.
 */
@Component
public class ScoreFactory {

    /** Convert a persisted Score entity to a response DTO. */
    public ScoreResponseDTO toResponseDTO(Score score) {
        return new ScoreResponseDTO(
                score.getScoreId(),
                score.getSongScore(),
                score.getSong() != null ? score.getSong().getSongId() : null
        );
    }
}
