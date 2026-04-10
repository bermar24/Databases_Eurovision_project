package com.dhbw.eurovision.controller;

import com.dhbw.eurovision.dto.request.ShowRequestDTO;
import com.dhbw.eurovision.dto.response.ShowResponseDTO;
import com.dhbw.eurovision.service.ShowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Show.
 * Base path: /api/shows
 */
@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    /** GET /api/shows */
    @GetMapping
    public ResponseEntity<List<ShowResponseDTO>> getAllShows() {
        return ResponseEntity.ok(showService.getAllShows());
    }

    /** GET /api/shows/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ShowResponseDTO> getShow(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getShowById(id));
    }

    /** POST /api/shows */
    @PostMapping
    public ResponseEntity<ShowResponseDTO> createShow(@RequestBody ShowRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.createShow(dto));
    }

    /** DELETE /api/shows/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }
}
