package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Show.
 * Extend with custom query methods as needed.
 * Spring Data JPA auto-generates: findAll, findById, save, deleteById, etc.
 */
@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    Optional<Show> findByShowName(String showName);
}
