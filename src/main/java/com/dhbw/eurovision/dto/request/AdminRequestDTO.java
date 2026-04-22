package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Inbound payload for creating an Admin. */
@Getter @Setter @NoArgsConstructor
public class AdminRequestDTO {
    private String countryCode;
    private Integer adminLevel;
    // Add shared user fields here if they are introduced in User.
}
