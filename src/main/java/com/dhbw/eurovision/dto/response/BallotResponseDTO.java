package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Outbound payload after a ballot is successfully submitted.
 * Returns the session ID and all 10 VoteLog entries created.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BallotResponseDTO {
    private String                   voteSessionId;
    private Long                     showId;
    private List<VoteLogResponseDTO> entries;       // 10 rows
}
