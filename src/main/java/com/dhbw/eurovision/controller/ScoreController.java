package com.dhbw.eurovision.controller;

import com.dhbw.eurovision.dto.response.ScoreResponseDTO;
import com.dhbw.eurovision.service.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Score.
 * Base path: /api/scores
 *
 * Score is now per (song, show) — every endpoint that deals with a single
 * score needs both a songId and a showId.
 */
@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    /** GET /api/scores — all scores across all shows */
    @GetMapping
    public ResponseEntity<List<ScoreResponseDTO>> getAllScores() {
        return ResponseEntity.ok(scoreService.getAllScores());
    }

    /** GET /api/scores/show/{showId} — leaderboard for one show */
    @GetMapping("/show/{showId}")
    public ResponseEntity<List<ScoreResponseDTO>> getScoresByShow(
            @PathVariable Long showId) {
        return ResponseEntity.ok(scoreService.getScoresByShow(showId));
    }

    /** GET /api/scores/song/{songId} — all show scores for one song */
    @GetMapping("/song/{songId}")
    public ResponseEntity<List<ScoreResponseDTO>> getScoresBySong(
            @PathVariable Long songId) {
        return ResponseEntity.ok(scoreService.getScoresBySong(songId));
    }

    /**
     * POST /api/scores/calculate-show/{showId}
     * Full Eurovision score calculation for a show:
     *   Grand Final  → jury aggregation + citizen aggregation
     *   Semi-Finals  → citizen aggregation only
     */
    @PostMapping("/calculate-show/{showId}")
    public ResponseEntity<List<ScoreResponseDTO>> calculateShowScores(
            @PathVariable Long showId) {
        return ResponseEntity.ok(scoreService.calculateShowScores(showId));
    }

    /**
     * POST /api/scores/calculate/{showId}/{songId}
     * Quick single-song recalculation within a specific show.
     */
    @PostMapping("/calculate/{showId}/{songId}")
    public ResponseEntity<ScoreResponseDTO> calculateScore(
            @PathVariable Long showId,
            @PathVariable Long songId) {
        return ResponseEntity.ok(scoreService.calculateScoreForSong(songId, showId));
    }

    /** POST /api/scores/calculate-all — recalculate every show */
    @PostMapping("/calculate-all")
    public ResponseEntity<List<ScoreResponseDTO>> calculateAll() {
        return ResponseEntity.ok(scoreService.calculateAllScores());
    }
}
