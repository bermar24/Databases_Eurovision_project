package com.dhbw.eurovision.controller;

import com.dhbw.eurovision.dto.request.JuryRequestDTO;
import com.dhbw.eurovision.dto.response.JuryResponseDTO;
import com.dhbw.eurovision.service.JuryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Jury.
 * Base path: /api/jury
 */
@RestController
@RequestMapping("/api/jury")
public class JuryController {

    private final JuryService juryService;

    public JuryController(JuryService juryService) {
        this.juryService = juryService;
    }

    /** GET /api/jury */
    @GetMapping
    public ResponseEntity<List<JuryResponseDTO>> getAllJury() {
        return ResponseEntity.ok(juryService.getAllJury());
    }

    /** GET /api/jury/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<JuryResponseDTO> getJury(@PathVariable Long id) {
        return ResponseEntity.ok(juryService.getJuryById(id));
    }

    /** POST /api/jury */
    @PostMapping
    public ResponseEntity<JuryResponseDTO> createJury(@RequestBody JuryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(juryService.createJury(dto));
    }

    /** DELETE /api/jury/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJury(@PathVariable Long id) {
        juryService.deleteJury(id);
        return ResponseEntity.noContent().build();
    }
}
