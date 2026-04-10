package com.dhbw.eurovision.factory;

import com.dhbw.eurovision.dto.request.ShowRequestDTO;
import com.dhbw.eurovision.dto.response.ShowResponseDTO;
import com.dhbw.eurovision.entity.Admin;
import com.dhbw.eurovision.entity.Show;
import com.dhbw.eurovision.entity.Song;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory for Show — converts between Entity <-> DTO.
 */
@Component
public class ShowFactory {

    /** Build a new Show entity from an inbound request DTO. */
    public Show toEntity(ShowRequestDTO dto) {
        Show show = new Show();
        // TODO: set showName, showDate, showType once entity fields are added
        return show;
    }

    /** Convert a persisted Show entity to a response DTO. */
    public ShowResponseDTO toResponseDTO(Show show) {
        List<Long> songIds = show.getSongs() == null ? List.of() :
                show.getSongs().stream().map(Song::getSongId).collect(Collectors.toList());

        List<Long> adminIds = show.getAdmins() == null ? List.of() :
                show.getAdmins().stream().map(Admin::getUserId).collect(Collectors.toList());

        return new ShowResponseDTO(show.getShowId(), songIds, adminIds);
    }
}
