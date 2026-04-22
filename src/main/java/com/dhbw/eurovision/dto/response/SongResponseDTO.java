package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Outbound payload for Song. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SongResponseDTO {
    private Long   songId;
    private String songTitle;
    private String singerName;
    private String countryCode;
    private String countryName;   // full name — convenient for display
}
