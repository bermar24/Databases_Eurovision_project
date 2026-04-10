package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Inbound payload for creating a Citizen voter. */
@Getter @Setter @NoArgsConstructor
public class CitizenRequestDTO {
    private String countryCode;
    // TODO: add username, email, phone when entity fields are added
}
