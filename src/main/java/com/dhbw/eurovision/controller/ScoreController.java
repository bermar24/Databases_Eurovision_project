package com.dhbw.eurovision.controller;

import com.dhbw.eurovision.dto.response.ScoreResponseDTO;
import com.dhbw.eurovision.service.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ScoreResponseDTO> getScoreForSong(
            @PathVariable Long songId) {
        return ResponseEntity.ok(scoreService.getScoreBySongId(songId));
    }
    
    /**
     * POST /api/scores/calculate-show/{showId}
     * Full score calculation for a show:
     *   Grand Final  → jury (direct) + citizen (country aggregation)
     *   Semi-Finals  → citizen (country aggregation) only
     */
    @PostMapping("/calculate-show/{showId}")
    public ResponseEntity<List<ScoreResponseDTO>> calculateShowScores(
            @PathVariable Long showId) {
        return ResponseEntity.ok(scoreService.calculateShowScores(showId));
    }

    /** POST /api/scores/calculate/{songId} — quick single-song recalculation */
    @PostMapping("/calculate/{songId}")
    public ResponseEntity<ScoreResponseDTO> calculateScore(
            @PathVariable Long songId) {
        return ResponseEntity.ok(scoreService.calculateScoreForSong(songId));
    }

    /** POST /api/scores/calculate-all */
    @PostMapping("/calculate-all")
    public ResponseEntity<List<ScoreResponseDTO>> calculateAll() {
        return ResponseEntity.ok(scoreService.calculateAllScores());
    }
}