package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Score.
 * Extend with custom query methods as needed.
 * Spring Data JPA auto-generates: findAll, findById, save, deleteById, etc.
 */
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    // TODO: add custom finders, e.g.:
    // List<Score> findBy...(String param);
}
