package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Inbound payload for creating an Admin. */
@Getter @Setter @NoArgsConstructor
public class AdminRequestDTO {
    private String countryCode;
    private Integer adminLevel;
    // TODO: add username, email when entity fields are added
}
