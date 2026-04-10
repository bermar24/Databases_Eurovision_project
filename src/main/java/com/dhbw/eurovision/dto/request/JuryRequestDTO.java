package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Inbound payload for creating a Jury member. */
@Getter @Setter @NoArgsConstructor
public class JuryRequestDTO {
    private String countryCode;
    private String professionalBg;
    // TODO: add username, email when entity fields are added
}
