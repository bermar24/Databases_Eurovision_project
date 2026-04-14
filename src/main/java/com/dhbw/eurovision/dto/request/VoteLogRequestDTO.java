package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound payload for casting a vote.
 * Rules (enforced in VoteLogService):
 *   - Exactly one of juryId / citizenId must be set.
 *   - points must be one of: 12, 10, 8, 7, 6, 5, 4, 3, 2, 1
 *   - Voter's country must differ from the song's country (no own-country vote).
 */
@Getter @Setter @NoArgsConstructor
public class VoteLogRequestDTO {
    private Long songId;
    private Long juryId;      // set if this is a jury vote
    private Long citizenId;   // set if this is a public/citizen vote
    private Integer points;      // must be: 12, 10, 8, 7, 6, 5, 4, 3, 2, or 1
}
