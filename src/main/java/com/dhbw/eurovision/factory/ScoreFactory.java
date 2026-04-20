package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.response.ScoreResponseDTO;
import com.dhbw.eurovision.entity.Score;
import org.springframework.stereotype.Component;

/**
 * Factory for Score.
 * Score has no RequestDTO — it is always calculated, never directly created.
 */
@Component
public class ScoreFactory {

    public ScoreResponseDTO toResponseDTO(Score score) {
        Long   songId      = score.getSong()  != null ? score.getSong().getSongId()          : null;
        Long   showId      = score.getShow()  != null ? score.getShow().getShowId()          : null;
        String showName    = score.getShow()  != null ? score.getShow().getShowName()        : null;
        String singerName  = score.getSong()  != null ? score.getSong().getSingerName()      : null;
        String countryCode = (score.getSong() != null && score.getSong().getCountry() != null)
                ? score.getSong().getCountry().getCountryCode() : null;

        return new ScoreResponseDTO(
                score.getScoreId(),
                score.getSongScore(),
                songId,
                showId,
                showName,
                singerName,
                countryCode
        );
    }
}