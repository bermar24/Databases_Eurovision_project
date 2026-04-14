package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.request.ShowRequestDTO;
import com.dhbw.eurovision.dto.response.ShowResponseDTO;
import com.dhbw.eurovision.entity.Show;
import com.dhbw.eurovision.entity.Song;
import com.dhbw.eurovision.factory.ShowFactory;
import com.dhbw.eurovision.repository.ShowRepository;
import com.dhbw.eurovision.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/** Service for Show. */
@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final SongRepository songRepository;
    private final ShowFactory showFactory;

    public ShowService(ShowRepository showRepository,
                       SongRepository songRepository,
                       ShowFactory showFactory) {
        this.showRepository = showRepository;
        this.songRepository = songRepository;
        this.showFactory = showFactory;
    }

    public List<ShowResponseDTO> getAllShows() {
        return showRepository.findAll()
                .stream()
                .map(showFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ShowResponseDTO getShowById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Show not found: " + id));
        return showFactory.toResponseDTO(show);
    }

    public ShowResponseDTO createShow(ShowRequestDTO dto) {
        Show show = showFactory.toEntity(dto);
        return showFactory.toResponseDTO(showRepository.save(show));
    }

    /**
     * Assign a Song to a Show — implements the show_song M:N join.
     * Used by the seed script to set up Semi-Final / Grand Final lineups.
     */
    public ShowResponseDTO addSongToShow(Long showId, Long songId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found: " + showId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found: " + songId));

        if (!show.getSongs().contains(song)) {
            show.getSongs().add(song);
            showRepository.save(show);
        }
        return showFactory.toResponseDTO(show);
    }

    public void deleteShow(Long id) {
        showRepository.deleteById(id);
    }
}
