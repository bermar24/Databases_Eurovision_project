package com.dhbw.eurovision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Outbound payload for an Admin. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AdminResponseDTO {
    private Long userId;
    private String countryCode;
    private Integer adminLevel;
    private List<Long> managedShowIds;
}
