package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.request.SongRequestDTO;
import com.dhbw.eurovision.dto.response.SongResponseDTO;
import com.dhbw.eurovision.entity.Country;
import com.dhbw.eurovision.entity.Song;
import org.springframework.stereotype.Component;

/**
 * Factory for Song — converts between Entity <-> DTO.
 * Country must be resolved by the Service before calling toEntity().
 */
@Component
public class SongFactory {

    /** Build a new Song entity. Country is resolved by the caller (SongService). */
    public Song toEntity(SongRequestDTO dto, Country country) {
        Song song = new Song();
        song.setSingerName(dto.getSingerName());
        song.setCountry(country);
        // TODO: set songTitle, language etc. once entity fields are added
        return song;
    }

    /** Convert a persisted Song entity to a response DTO. */
    public SongResponseDTO toResponseDTO(Song song) {
        return new SongResponseDTO(
                song.getSongId(),
                song.getSingerName(),
                song.getCountry() != null ? song.getCountry().getCountryCode() : null
        );
    }
}
