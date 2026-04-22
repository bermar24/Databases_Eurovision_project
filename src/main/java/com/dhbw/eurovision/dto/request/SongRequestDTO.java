package com.dhbw.eurovision.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Inbound payload for creating a Song. */
@Getter @Setter @NoArgsConstructor
public class SongRequestDTO {
    private String songTitle;
    private String singerName;
    private String countryCode;
}
