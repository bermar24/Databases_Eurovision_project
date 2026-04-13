package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Inbound payload for creating a Show. */
@Getter @Setter @NoArgsConstructor
public class ShowRequestDTO {
    private String showName;
    // TODO: add showDate, showType
    // private String showType;  // e.g. "SEMI_FINAL_1", "GRAND_FINAL"
}
