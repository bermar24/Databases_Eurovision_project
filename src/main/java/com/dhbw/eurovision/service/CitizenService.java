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
        return citizenRepository.findAll()
                .stream()
                .map(citizenFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CitizenResponseDTO getCitizenById(Long id) {
        Citizen citizen = citizenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Citizen not found: " + id));
        return citizenFactory.toResponseDTO(citizen);
    }

    public CitizenResponseDTO createCitizen(CitizenRequestDTO dto) {
        Country country = countryRepository.findById(dto.getCountryCode())
                .orElseThrow(() -> new RuntimeException("Country not found: " + dto.getCountryCode()));
        Citizen citizen = citizenFactory.toEntity(dto, country);
        return citizenFactory.toResponseDTO(citizenRepository.save(citizen));
    }

    public void deleteCitizen(Long id) {
        citizenRepository.deleteById(id);
    }
}
