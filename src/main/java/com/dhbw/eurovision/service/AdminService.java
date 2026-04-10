package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.request.AdminRequestDTO;
import com.dhbw.eurovision.dto.response.AdminResponseDTO;
import com.dhbw.eurovision.entity.Admin;
import com.dhbw.eurovision.entity.Country;
import com.dhbw.eurovision.entity.Show;
import com.dhbw.eurovision.factory.AdminFactory;
import com.dhbw.eurovision.repository.AdminRepository;
import com.dhbw.eurovision.repository.CountryRepository;
import com.dhbw.eurovision.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/** Service for Admin — business logic layer. */
@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final CountryRepository countryRepository;
    private final ShowRepository showRepository;
    private final AdminFactory adminFactory;

    public AdminService(AdminRepository adminRepository,
                        CountryRepository countryRepository,
                        ShowRepository showRepository,
                        AdminFactory adminFactory) {
        this.adminRepository = adminRepository;
        this.countryRepository = countryRepository;
        this.showRepository = showRepository;
        this.adminFactory = adminFactory;
    }

    public List<AdminResponseDTO> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(adminFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    public AdminResponseDTO getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + id));
        return adminFactory.toResponseDTO(admin);
    }

    public AdminResponseDTO createAdmin(AdminRequestDTO dto) {
        Country country = countryRepository.findById(dto.getCountryCode())
                .orElseThrow(() -> new RuntimeException("Country not found: " + dto.getCountryCode()));
        Admin admin = adminFactory.toEntity(dto, country);
        return adminFactory.toResponseDTO(adminRepository.save(admin));
    }

    /**
     * Assign an Admin to manage a Show — the EERM "Manage" M:N relationship.
     */
    public AdminResponseDTO assignShowToAdmin(Long adminId, Long showId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + adminId));
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found: " + showId));
        admin.getManagedShows().add(show);
        return adminFactory.toResponseDTO(adminRepository.save(admin));
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
}
