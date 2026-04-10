package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Outbound payload for Show. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ShowResponseDTO {
    private Long showId;
    private List<Long> songIds;    // IDs of songs in this show
    private List<Long> adminIds;   // IDs of admins managing this show
    // TODO: add showName, showDate, showType when entity fields are added
}
