package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.request.JuryRequestDTO;
import com.dhbw.eurovision.dto.response.JuryResponseDTO;
import com.dhbw.eurovision.entity.Country;
import com.dhbw.eurovision.entity.Jury;
import com.dhbw.eurovision.factory.JuryFactory;
import com.dhbw.eurovision.repository.CountryRepository;
import com.dhbw.eurovision.repository.JuryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/** Service for Jury — business logic layer. */
@Service
public class JuryService {

    private final JuryRepository juryRepository;
    private final CountryRepository countryRepository;
    private final JuryFactory juryFactory;

    public JuryService(JuryRepository juryRepository,
                       CountryRepository countryRepository,
                       JuryFactory juryFactory) {
        this.juryRepository = juryRepository;
        this.countryRepository = countryRepository;
        this.juryFactory = juryFactory;
    }

    public List<JuryResponseDTO> getAllJury() {
        return juryRepository.findAll()
                .stream()
                .map(juryFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    public JuryResponseDTO getJuryById(Long id) {
        Jury jury = juryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jury member not found: " + id));
        return juryFactory.toResponseDTO(jury);
    }

    public JuryResponseDTO createJury(JuryRequestDTO dto) {
        Country country = countryRepository.findById(dto.getCountryCode())
                .orElseThrow(() -> new RuntimeException("Country not found: " + dto.getCountryCode()));
        Jury jury = juryFactory.toEntity(dto, country);
        return juryFactory.toResponseDTO(juryRepository.save(jury));
    }

    public void deleteJury(Long id) {
        juryRepository.deleteById(id);
    }
}
