package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.request.ShowRequestDTO;
import com.dhbw.eurovision.dto.response.ShowResponseDTO;
import com.dhbw.eurovision.entity.Show;
import com.dhbw.eurovision.factory.ShowFactory;
import com.dhbw.eurovision.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Show.
 */
@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowFactory showFactory;

    public ShowService(ShowRepository showRepository, ShowFactory showFactory) {
        this.showRepository = showRepository;
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

    public void deleteShow(Long id) {
        showRepository.deleteById(id);
    }

    // TODO: assignSongToShow, assignAdminToShow, etc.
}
