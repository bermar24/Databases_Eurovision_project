package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Show.
 * Extend with custom query methods as needed.
 * Spring Data JPA auto-generates: findAll, findById, save, deleteById, etc.
 */
@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    // TODO: add custom finders, e.g.:
    // List<Show> findBy...(String param);
}
