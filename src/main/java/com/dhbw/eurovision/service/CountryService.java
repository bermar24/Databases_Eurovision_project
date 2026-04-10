package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.request.CountryRequestDTO;
import com.dhbw.eurovision.dto.response.CountryResponseDTO;
import com.dhbw.eurovision.entity.Country;
import com.dhbw.eurovision.factory.CountryFactory;
import com.dhbw.eurovision.repository.CountryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Country — business logic layer.
 */
@Service
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryFactory countryFactory;

    public CountryService(CountryRepository countryRepository, CountryFactory countryFactory) {
        this.countryRepository = countryRepository;
        this.countryFactory = countryFactory;
    }

    public List<CountryResponseDTO> getAllCountries() {
        return countryRepository.findAll()
                .stream()
                .map(countryFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CountryResponseDTO getCountryByCode(String code) {
        Country country = countryRepository.findById(code)
                .orElseThrow(() -> new RuntimeException("Country not found: " + code));
        return countryFactory.toResponseDTO(country);
    }

    public CountryResponseDTO createCountry(CountryRequestDTO dto) {
        Country country = countryFactory.toEntity(dto);
        return countryFactory.toResponseDTO(countryRepository.save(country));
    }

    public void deleteCountry(String code) {
        countryRepository.deleteById(code);
    }

    // TODO: add updateCountry, findByName, etc.
}
