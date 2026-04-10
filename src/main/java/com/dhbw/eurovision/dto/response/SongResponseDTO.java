package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Outbound payload for Song. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SongResponseDTO {
    private Long songId;
    private String singerName;
    private String countryCode;   // just the FK code, not the full Country object
    // TODO: expose songTitle, scoreId, etc. when those fields are added
}
