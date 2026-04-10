package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Outbound payload for a Jury member. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class JuryResponseDTO {
    private Long userId;
    private String countryCode;
    private String professionalBg;
}
