package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One entry in a voting ballot: a song and the points awarded to it.
 * points must be one of: 12, 10, 8, 7, 6, 5, 4, 3, 2, 1
 */
@Getter @Setter @NoArgsConstructor
public class BallotEntryDTO {
    private Long    songId;
    private Integer points;
}
