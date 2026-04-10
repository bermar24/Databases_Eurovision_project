package com.dhbw.eurovision.controller;

import com.dhbw.eurovision.dto.request.SongRequestDTO;
import com.dhbw.eurovision.dto.response.SongResponseDTO;
import com.dhbw.eurovision.service.SongService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Song.
 * Base path: /api/songs
 */
@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    /** GET /api/songs */
    @GetMapping
    public ResponseEntity<List<SongResponseDTO>> getAllSongs() {
        return ResponseEntity.ok(songService.getAllSongs());
    }

    /** GET /api/songs/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<SongResponseDTO> getSong(@PathVariable Long id) {
        return ResponseEntity.ok(songService.getSongById(id));
    }

    /** POST /api/songs */
    @PostMapping
    public ResponseEntity<SongResponseDTO> createSong(@RequestBody SongRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(songService.createSong(dto));
    }

    /** DELETE /api/songs/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }
}
