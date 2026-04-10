package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.request.AdminRequestDTO;
import com.dhbw.eurovision.dto.response.AdminResponseDTO;
import com.dhbw.eurovision.entity.Admin;
import com.dhbw.eurovision.entity.Country;
import com.dhbw.eurovision.entity.Show;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory for Admin — converts between Entity <-> DTO.
 */
@Component
public class AdminFactory {

    /** Build a new Admin entity. Country must be resolved by the caller. */
    public Admin toEntity(AdminRequestDTO dto, Country country) {
        Admin admin = new Admin();
        admin.setCountry(country);
        admin.setAdminLevel(dto.getAdminLevel());
        // TODO: set username, email once User entity fields are added
        return admin;
    }

    /** Convert a persisted Admin entity to a response DTO. */
    public AdminResponseDTO toResponseDTO(Admin admin) {
        List<Long> showIds = admin.getManagedShows() == null ? List.of() :
                admin.getManagedShows().stream().map(Show::getShowId).collect(Collectors.toList());

        return new AdminResponseDTO(
                admin.getUserId(),
                admin.getCountry() != null ? admin.getCountry().getCountryCode() : null,
                admin.getAdminLevel(),
                showIds
        );
    }
}
