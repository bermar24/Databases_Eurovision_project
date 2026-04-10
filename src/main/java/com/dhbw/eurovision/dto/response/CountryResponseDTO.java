package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Outbound payload for Country. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CountryResponseDTO {
    private String countryCode;
    private String countryName;
}
