package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Outbound payload for a Citizen voter. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CitizenResponseDTO {
    private Long userId;
    private String countryCode;
    private String phoneNumber;
}
