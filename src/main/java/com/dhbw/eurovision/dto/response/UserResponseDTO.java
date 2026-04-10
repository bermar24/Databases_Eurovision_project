package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic outbound payload for any User (base fields only).
 * For subtype-specific fields use JuryResponseDTO, CitizenResponseDTO, AdminResponseDTO.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserResponseDTO {
    private Long userId;
    private String countryCode;
    private String userType;    // "JURY", "CITIZEN", or "ADMIN"
    // TODO: add username, email when entity fields are added
}
