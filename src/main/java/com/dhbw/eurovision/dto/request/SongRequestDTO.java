package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Inbound payload for creating a Song. */
@Getter @Setter @NoArgsConstructor
public class SongRequestDTO {
    private String singerName;
    private String countryCode;   // FK — which country this song represents
    // TODO: add songTitle, language, etc. once agreed with your team
}
