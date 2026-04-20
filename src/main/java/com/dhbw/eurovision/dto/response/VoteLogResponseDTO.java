package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Outbound payload for a single vote entry. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VoteLogResponseDTO {
    private Long voteLogId;
    private Long songId;
    private Long showId;
    private Long juryId;      // null if citizen vote
    private Long citizenId;   // null if jury vote
    private Integer points;      // 12, 10, 8, 7, 6, 5, 4, 3, 2, or 1
    private String  voteSessionId;
}