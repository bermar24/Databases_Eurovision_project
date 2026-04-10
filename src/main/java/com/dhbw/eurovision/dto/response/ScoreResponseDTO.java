package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Outbound payload for a Song's score. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScoreResponseDTO {
    private Long scoreId;
    private Integer songScore;
    private Long songId;
}
