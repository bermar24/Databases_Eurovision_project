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
    private Long juryId;      // null if citizen vote
    private Long citizenId;   // null if jury vote
    // TODO: add Integer points when the field is added to VoteLog entity
}
