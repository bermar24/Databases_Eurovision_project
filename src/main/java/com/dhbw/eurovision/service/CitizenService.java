package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.request.CitizenRequestDTO;
import com.dhbw.eurovision.dto.response.CitizenResponseDTO;
import com.dhbw.eurovision.entity.Citizen;
import com.dhbw.eurovision.entity.Country;
import com.dhbw.eurovision.factory.CitizenFactory;
import com.dhbw.eurovision.repository.CitizenRepository;
import com.dhbw.eurovision.repository.CountryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
/** Service for Citizen — business logic layer. */
@Service
public class CitizenService {

    private final CitizenRepository citizenRepository;
    private final CountryRepository countryRepository;
    private final CitizenFactory citizenFactory;

    public CitizenService(CitizenRepository citizenRepository,
                          CountryRepository countryRepository,
                          CitizenFactory citizenFactory) {
        this.citizenRepository = citizenRepository;
        this.countryRepository = countryRepository;
        this.citizenFactory = citizenFactory;
    }

    public List<CitizenResponseDTO> getAllCitizens() {
        return citizenRepository.findAll().stream()
                .map(citizenFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CitizenResponseDTO getCitizenById(Long id) {
        return citizenFactory.toResponseDTO(
                citizenRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Citizen not found: " + id)));
    }

    /**
     * Look up by phone number — return existing citizen or create a new one.
     * This is the primary registration/login flow: same phone = same voter.
     */
    public CitizenResponseDTO findOrCreateByPhone(CitizenRequestDTO dto) {
        if (dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        Optional<Citizen> existingCitizen = findByPhoneNumberFlexible(dto.getPhoneNumber());
        if (existingCitizen.isPresent()) {
            return citizenFactory.toResponseDTO(existingCitizen.get());
        }

        if (dto.getCountryCode() == null || dto.getCountryCode().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Country code is required for new phone numbers.");
        }

        dto.setPhoneNumber(normalizePhoneNumber(dto.getPhoneNumber()));
        return citizenRepository.findByPhoneNumber(dto.getPhoneNumber())
                .map(citizenFactory::toResponseDTO)
                .orElseGet(() -> createCitizen(dto));
    }

    /** Look up an existing citizen by phone — 404 if not found */
    public CitizenResponseDTO getByPhoneNumber(String phoneNumber) {
        return citizenFactory.toResponseDTO(
                findByPhoneNumberFlexible(phoneNumber)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No citizen found with phone: " + phoneNumber)));
    }

    public CitizenResponseDTO createCitizen(CitizenRequestDTO dto) {
        dto.setPhoneNumber(normalizePhoneNumber(dto.getPhoneNumber()));
        if (citizenRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            // Phone already registered — return existing instead of duplicating
            return citizenFactory.toResponseDTO(
                    citizenRepository.findByPhoneNumber(dto.getPhoneNumber())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Phone exists but citizen record could not be loaded.")));
        }
        Country country = countryRepository.findById(dto.getCountryCode())
                .orElseThrow(() -> new RuntimeException("Country not found: " + dto.getCountryCode()));
        return citizenFactory.toResponseDTO(
                citizenRepository.save(citizenFactory.toEntity(dto, country)));
    }

    private Optional<Citizen> findByPhoneNumberFlexible(String phoneNumber) {
        String trimmed = phoneNumber == null ? "" : phoneNumber.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }

        String normalized = normalizePhoneNumber(trimmed);
        Optional<Citizen> byNormalized = citizenRepository.findByPhoneNumber(normalized);
        if (byNormalized.isPresent()) {
            return byNormalized;
        }

        if (!normalized.equals(trimmed)) {
            return citizenRepository.findByPhoneNumber(trimmed);
        }

        return Optional.empty();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber == null ? null : phoneNumber.trim().replaceAll("\\s+", "");
    }

    public void deleteCitizen(Long id) {
        citizenRepository.deleteById(id);
    }
}