package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.response.ScoreResponseDTO;
import com.dhbw.eurovision.entity.Score;
import org.springframework.stereotype.Component;

@Component
public class ScoreFactory {

    public ScoreResponseDTO toResponseDTO(Score score) {
        Long   songId      = score.getSong()  != null ? score.getSong().getSongId()          : null;
        Long   showId      = score.getShow()  != null ? score.getShow().getShowId()          : null;
        String showName    = score.getShow()  != null ? score.getShow().getShowName()        : null;
        // singerName field in DTO reused to carry "Song Title (Singer)" for convenience
        String singerName  = null;
        if (score.getSong() != null) {
            String title  = score.getSong().getSongTitle();
            String singer = score.getSong().getSingerName();
            singerName = (title != null)
                    ? title + " (" + singer + ")"
                    : singer;
        }
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