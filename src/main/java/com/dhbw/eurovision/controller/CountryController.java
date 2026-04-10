package com.dhbw.eurovision.controller;

import com.dhbw.eurovision.dto.request.CountryRequestDTO;
import com.dhbw.eurovision.dto.response.CountryResponseDTO;
import com.dhbw.eurovision.service.CountryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Country.
 * Base path: /api/countries
 */
@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    /** GET /api/countries — list all countries */
    @GetMapping
    public ResponseEntity<List<CountryResponseDTO>> getAllCountries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }

    /** GET /api/countries/{code} — get one country by its ISO code */
    @GetMapping("/{code}")
    public ResponseEntity<CountryResponseDTO> getCountry(@PathVariable String code) {
        return ResponseEntity.ok(countryService.getCountryByCode(code));
    }

    /** POST /api/countries — create a new country */
    @PostMapping
    public ResponseEntity<CountryResponseDTO> createCountry(@RequestBody CountryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(countryService.createCountry(dto));
    }

    /** DELETE /api/countries/{code} — remove a country */
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCountry(@PathVariable String code) {
        countryService.deleteCountry(code);
        return ResponseEntity.noContent().build();
    }
}
