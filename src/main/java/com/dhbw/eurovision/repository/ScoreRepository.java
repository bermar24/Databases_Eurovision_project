package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.Score;
import com.dhbw.eurovision.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Score.
*/
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    Optional<Score> findBySong(Song song);

    Optional<Score> findBySong_SongId(Long songId);
}
