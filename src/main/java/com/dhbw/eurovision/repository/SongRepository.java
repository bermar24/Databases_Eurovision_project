package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Song.
 */
@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByCountry_CountryCode(String countryCode);
}