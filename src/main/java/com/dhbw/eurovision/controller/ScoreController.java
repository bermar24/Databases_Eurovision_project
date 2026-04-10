package com.dhbw.eurovision.controller;

import com.dhbw.eurovision.dto.response.ScoreResponseDTO;
import com.dhbw.eurovision.service.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Score.
 * Base path: /api/scores
 */
@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    /** GET /api/scores — list all scores (leaderboard) */
    @GetMapping
    public ResponseEntity<List<ScoreResponseDTO>> getAllScores() {
        return ResponseEntity.ok(scoreService.getAllScores());
    }

    /** GET /api/scores/song/{songId} — get the score for a specific song */
    @GetMapping("/song/{songId}")
    public ResponseEntity<ScoreResponseDTO> getScoreForSong(@PathVariable Long songId) {
        return ResponseEntity.ok(scoreService.getScoreBySongId(songId));
    }

    /**
     * POST /api/scores/calculate/{songId} — trigger score calculation for a song.
     * Aggregates all VoteLogs for the song into a final Score.
     */
    @PostMapping("/calculate/{songId}")
    public ResponseEntity<ScoreResponseDTO> calculateScore(@PathVariable Long songId) {
        return ResponseEntity.ok(scoreService.calculateScoreForSong(songId));
    }
}
