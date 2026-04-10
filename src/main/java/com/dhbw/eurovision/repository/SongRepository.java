package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Song.
 * Extend with custom query methods as needed.
 * Spring Data JPA auto-generates: findAll, findById, save, deleteById, etc.
 */
@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // TODO: add custom finders, e.g.:
    // List<Song> findBy...(String param);
}
