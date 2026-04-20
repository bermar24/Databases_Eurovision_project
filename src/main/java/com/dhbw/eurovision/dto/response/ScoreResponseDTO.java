package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outbound payload for a Score.
 * Now includes showId so the caller knows which show this score belongs to.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScoreResponseDTO {
    private Long    scoreId;
    private Integer songScore;
    private Long    songId;
    private Long    showId;     
    private String  showName;
    private String  singerName;
    private String  countryCode;
}
