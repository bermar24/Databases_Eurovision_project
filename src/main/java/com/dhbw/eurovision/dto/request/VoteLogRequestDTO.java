package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound payload for casting a vote.
 * Either juryId OR citizenId must be set — not both, not neither.
 */
@Getter @Setter @NoArgsConstructor
public class VoteLogRequestDTO {
    private Long songId;
    private Long juryId;      // set if this is a jury vote
    private Long citizenId;   // set if this is a public/citizen vote
    // TODO: add Integer points when your team defines the voting point scale
}
