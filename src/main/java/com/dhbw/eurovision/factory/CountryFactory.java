package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.request.CountryRequestDTO;
import com.dhbw.eurovision.dto.response.CountryResponseDTO;
import com.dhbw.eurovision.entity.Country;
import org.springframework.stereotype.Component;

/**
 * Factory for Country — converts between Entity <-> DTO.
 * Keeps mapping logic out of Service and Controller.
 */
@Component
public class CountryFactory {

    /** Build a new Country entity from an inbound request DTO. */
    public Country toEntity(CountryRequestDTO dto) {
        Country country = new Country();
        country.setCountryCode(dto.getCountryCode());
        country.setCountryName(dto.getCountryName());
        return country;
    }

    /** Convert a persisted Country entity to a response DTO. */
    public CountryResponseDTO toResponseDTO(Country country) {
        return new CountryResponseDTO(
                country.getCountryCode(),
                country.getCountryName()
        );
    }
}
