package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.request.CitizenRequestDTO;
import com.dhbw.eurovision.dto.response.CitizenResponseDTO;
import com.dhbw.eurovision.entity.Citizen;
import com.dhbw.eurovision.entity.Country;
import com.dhbw.eurovision.factory.CitizenFactory;
import com.dhbw.eurovision.repository.CitizenRepository;
import com.dhbw.eurovision.repository.CountryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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
        return citizenRepository.findByPhoneNumber(dto.getPhoneNumber())
                .map(citizenFactory::toResponseDTO)
                .orElseGet(() -> createCitizen(dto));
    }

    /** Look up an existing citizen by phone — 404 if not found */
    public CitizenResponseDTO getByPhoneNumber(String phoneNumber) {
        return citizenFactory.toResponseDTO(
                citizenRepository.findByPhoneNumber(phoneNumber)
                        .orElseThrow(() -> new RuntimeException(
                                "No citizen found with phone: " + phoneNumber)));
    }

    public CitizenResponseDTO createCitizen(CitizenRequestDTO dto) {
        if (citizenRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            // Phone already registered — return existing instead of duplicating
            return citizenFactory.toResponseDTO(
                    citizenRepository.findByPhoneNumber(dto.getPhoneNumber()).get());
        }
        Country country = countryRepository.findById(dto.getCountryCode())
                .orElseThrow(() -> new RuntimeException("Country not found: " + dto.getCountryCode()));
        return citizenFactory.toResponseDTO(
                citizenRepository.save(citizenFactory.toEntity(dto, country)));
    }
    public void deleteCitizen(Long id) {
        citizenRepository.deleteById(id);
    }
}