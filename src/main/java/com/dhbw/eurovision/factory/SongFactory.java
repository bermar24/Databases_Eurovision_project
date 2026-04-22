package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.request.SongRequestDTO;
import com.dhbw.eurovision.dto.response.SongResponseDTO;
import com.dhbw.eurovision.entity.Country;
import com.dhbw.eurovision.entity.Song;
import org.springframework.stereotype.Component;

@Component
public class SongFactory {

    public Song toEntity(SongRequestDTO dto, Country country) {
        Song song = new Song();
        song.setSongTitle(dto.getSongTitle());
        song.setSingerName(dto.getSingerName());
        song.setCountry(country);
        return song;
    }

    public SongResponseDTO toResponseDTO(Song song) {
        String countryName = (song.getCountry() != null)
                ? song.getCountry().getCountryName() : null;
        String countryCode = (song.getCountry() != null)
                ? song.getCountry().getCountryCode() : null;
        return new SongResponseDTO(
                song.getSongId(),
                song.getSongTitle(),
                song.getSingerName(),
                countryCode,
                countryName
        );
    }
}
