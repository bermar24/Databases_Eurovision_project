package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.request.CitizenRequestDTO;
import com.dhbw.eurovision.dto.response.CitizenResponseDTO;
import com.dhbw.eurovision.entity.Citizen;
import com.dhbw.eurovision.entity.Country;
import org.springframework.stereotype.Component;

/**
 * Factory for Citizen — converts between Entity <-> DTO.
 */
@Component
public class CitizenFactory {

    /** Build a new Citizen entity. Country must be resolved by the caller. */
    public Citizen toEntity(CitizenRequestDTO dto, Country country) {
        Citizen citizen = new Citizen();
        citizen.setCountry(country);
        // TODO: set username, email, phone once entity fields are added
        return citizen;
    }

    /** Convert a persisted Citizen entity to a response DTO. */
    public CitizenResponseDTO toResponseDTO(Citizen citizen) {
        return new CitizenResponseDTO(
                citizen.getUserId(),
                citizen.getCountry() != null ? citizen.getCountry().getCountryCode() : null
        );
    }
}
