package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.request.JuryRequestDTO;
import com.dhbw.eurovision.dto.response.JuryResponseDTO;
import com.dhbw.eurovision.entity.Country;
import com.dhbw.eurovision.entity.Jury;
import org.springframework.stereotype.Component;

/**
 * Factory for Jury — converts between Entity <-> DTO.
 */
@Component
public class JuryFactory {

    /** Build a new Jury entity. Country must be resolved by the caller. */
    public Jury toEntity(JuryRequestDTO dto, Country country) {
        Jury jury = new Jury();
        jury.setCountry(country);
        jury.setProfessionalBg(dto.getProfessionalBg());
        // TODO: set username, email once User entity fields are added
        return jury;
    }

    /** Convert a persisted Jury entity to a response DTO. */
    public JuryResponseDTO toResponseDTO(Jury jury) {
        return new JuryResponseDTO(
                jury.getUserId(),
                jury.getCountry() != null ? jury.getCountry().getCountryCode() : null,
                jury.getProfessionalBg()
        );
    }
}
