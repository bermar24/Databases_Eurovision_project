package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Inbound payload for submitting a complete Eurovision ballot.
 *
 * Rules (enforced in VoteLogService):
 *   - Exactly one of juryId / citizenId must be set.
 *   - showId is required.
 *   - votes must contain exactly 10 entries using points: 12,10,8,7,6,5,4,3,2,1 each once.
 *   - No own-country song allowed.
 *   - All songs must be assigned to the selected show.
 *   - Voter's country must be eligible for the selected show.
 *   - Voter must not have already voted in this show.
 */
@Getter @Setter @NoArgsConstructor
public class BallotRequestDTO {
    private Long              showId;
    private Long              juryId;      // set for jury ballot
    private Long              citizenId;   // set for citizen/televote ballot
    private List<BallotEntryDTO> votes;    // exactly 10 entries
}
