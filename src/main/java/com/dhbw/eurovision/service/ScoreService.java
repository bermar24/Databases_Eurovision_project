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
 * Service for Score.
 * The "Calculate" relationship from the EERM lives here —
 * this service aggregates VoteLogs into a Score for a Song.
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

    public List<ScoreResponseDTO> getAllScores() {
        return scoreRepository.findAll()
                .stream()
                .map(scoreFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ScoreResponseDTO getScoreBySongId(Long songId) {
        // TODO: implement lookup by song FK
        throw new UnsupportedOperationException("TODO: implement getScoreBySongId");
    }

    /**
     * Recalculate and persist the Score for a given Song
     * by summing all its VoteLog entries.
     * TODO: implement after VoteLog.points field is added.
     */
    public ScoreResponseDTO calculateScoreForSong(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found: " + songId));

        // TODO: sum voteLog.getPoints() for this song
        // int total = song.getVoteLogs().stream().mapToInt(VoteLog::getPoints).sum();
        // Score score = scoreRepository.findBySong(song).orElse(new Score());
        // score.setSong(song);
        // score.setSongScore(total);
        // return scoreFactory.toResponseDTO(scoreRepository.save(score));

        throw new UnsupportedOperationException("TODO: implement score calculation");
    }
}
