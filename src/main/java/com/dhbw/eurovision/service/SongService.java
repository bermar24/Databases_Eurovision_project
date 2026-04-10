package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.request.SongRequestDTO;
import com.dhbw.eurovision.dto.response.SongResponseDTO;
import com.dhbw.eurovision.entity.Country;
import com.dhbw.eurovision.entity.Song;
import com.dhbw.eurovision.factory.SongFactory;
import com.dhbw.eurovision.repository.CountryRepository;
import com.dhbw.eurovision.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Song — business logic layer.
 */
@Service
public class SongService {

    private final SongRepository songRepository;
    private final CountryRepository countryRepository;
    private final SongFactory songFactory;

    public SongService(SongRepository songRepository,
                       CountryRepository countryRepository,
                       SongFactory songFactory) {
        this.songRepository = songRepository;
        this.countryRepository = countryRepository;
        this.songFactory = songFactory;
    }

    public List<SongResponseDTO> getAllSongs() {
        return songRepository.findAll()
                .stream()
                .map(songFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    public SongResponseDTO getSongById(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found: " + id));
        return songFactory.toResponseDTO(song);
    }

    public SongResponseDTO createSong(SongRequestDTO dto) {
        Country country = countryRepository.findById(dto.getCountryCode())
                .orElseThrow(() -> new RuntimeException("Country not found: " + dto.getCountryCode()));
        Song song = songFactory.toEntity(dto, country);
        return songFactory.toResponseDTO(songRepository.save(song));
    }

    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }

    // TODO: getSongsByCountry, getSongsByShow, etc.
}
