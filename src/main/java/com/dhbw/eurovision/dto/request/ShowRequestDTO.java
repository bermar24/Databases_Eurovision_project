package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Inbound payload for creating a Show. */
@Getter @Setter @NoArgsConstructor
public class ShowRequestDTO {
    private String showName;
    // Add extra show fields here if Show is extended.
    // private String showType;  // e.g. "SEMI_FINAL_1", "GRAND_FINAL"
}
