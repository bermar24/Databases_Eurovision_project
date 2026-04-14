package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.response.ScoreResponseDTO;
import com.dhbw.eurovision.entity.Score;
import com.dhbw.eurovision.entity.Song;
import com.dhbw.eurovision.factory.ScoreFactory;
import com.dhbw.eurovision.repository.ScoreRepository;
import com.dhbw.eurovision.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Score — implements the EERM "Calculate" relationship.
 *
 * Score = sum of all VoteLog.points for a given Song.
 * Points scale: 12, 10, 8, 7, 6, 5, 4, 3, 2, 1 (Eurovision standard).
 */
@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final SongRepository songRepository;
    private final ScoreFactory scoreFactory;

    public ScoreService(ScoreRepository scoreRepository,
                        SongRepository songRepository,
                        ScoreFactory scoreFactory) {
        this.scoreRepository = scoreRepository;
        this.songRepository = songRepository;
        this.scoreFactory = scoreFactory;
    }

    /** Full leaderboard — all scored songs. */
    public List<ScoreResponseDTO> getAllScores() {
        return scoreRepository.findAll()
                .stream()
                .map(scoreFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    /** Score for one specific song. */
    public ScoreResponseDTO getScoreBySongId(Long songId) {
        Score score = scoreRepository.findBySong_SongId(songId)
                .orElseThrow(() -> new RuntimeException("No score found for song: " + songId));
        return scoreFactory.toResponseDTO(score);
    }

    /**
     * Recalculate and persist the Score for a given Song
     * Aggregates all VoteLog.points for this song (Eurovision scale: 12,10,8,...,1).
     */
    public ScoreResponseDTO calculateScoreForSong(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found: " + songId));

        // Sum all points from every VoteLog entry for this song
        int total = song.getVoteLogs()
                .stream()
                .mapToInt(vl -> vl.getPoints() != null ? vl.getPoints() : 0)
                .sum();

        // Upsert — update existing Score row or create a new one
        Score score = scoreRepository.findBySong(song).orElse(new Score());
        score.setSong(song);
        score.setSongScore(total);

        return scoreFactory.toResponseDTO(scoreRepository.save(score));
    }

    /** Recalculate scores for ALL songs at once. */
    public List<ScoreResponseDTO> calculateAllScores() {
        return songRepository.findAll()
                .stream()
                .map(song -> calculateScoreForSong(song.getSongId()))
                .collect(Collectors.toList());
    }
}