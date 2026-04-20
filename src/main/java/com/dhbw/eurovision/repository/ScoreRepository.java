package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.Score;
import com.dhbw.eurovision.entity.Show;
import com.dhbw.eurovision.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Score.
 * Primary lookup is by (song, show) pair — the natural business key.
 */
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    /** Find the score for a specific song in a specific show. */
    Optional<Score> findBySongAndShow(Song song, Show show);

    /** All scores for a specific show — used for leaderboard per show. */
    List<Score> findByShow_ShowId(Long showId);

    /** All scores for a specific song across all shows. */
    List<Score> findBySong_SongId(Long songId);

    /** Check if a score already exists for this (song, show) pair. */
    boolean existsBySongAndShow(Song song, Show show);
}