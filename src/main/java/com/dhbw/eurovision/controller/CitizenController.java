package com.dhbw.eurovision.controller;

import com.dhbw.eurovision.dto.request.CitizenRequestDTO;
import com.dhbw.eurovision.dto.response.CitizenResponseDTO;
import com.dhbw.eurovision.service.CitizenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citizens")
public class CitizenController {

    private final CitizenService citizenService;

    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    /** GET /api/citizens */
    @GetMapping
    public ResponseEntity<List<CitizenResponseDTO>> getAllCitizens() {
        return ResponseEntity.ok(citizenService.getAllCitizens());
    }

    /** GET /api/citizens/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<CitizenResponseDTO> getCitizen(@PathVariable Long id) {
        return ResponseEntity.ok(citizenService.getCitizenById(id));
    }

    /**
     * GET /api/citizens/by-phone/{phoneNumber}
     * Look up an existing citizen by phone number.
     */
    @GetMapping("/by-phone/{phoneNumber}")
    public ResponseEntity<CitizenResponseDTO> getByPhone(
            @PathVariable String phoneNumber) {
        return ResponseEntity.ok(citizenService.getByPhoneNumber(phoneNumber));
    }

    /**
     * POST /api/citizens
     * Register or retrieve a citizen by phone number.
     * If phone already exists → returns existing record (idempotent).
     */
    @PostMapping
    public ResponseEntity<CitizenResponseDTO> findOrCreate(
            @RequestBody CitizenRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(citizenService.findOrCreateByPhone(dto));
    }

    /** DELETE /api/citizens/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCitizen(@PathVariable Long id) {
        citizenService.deleteCitizen(id);
        return ResponseEntity.noContent().build();
    }
}
