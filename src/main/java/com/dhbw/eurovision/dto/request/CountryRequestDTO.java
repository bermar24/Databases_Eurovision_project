package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Inbound payload for creating/updating a Country. */
@Getter @Setter @NoArgsConstructor
public class CountryRequestDTO {
    private String countryCode;   // e.g. "DE"
    private String countryName;   // e.g. "Germany"
}
