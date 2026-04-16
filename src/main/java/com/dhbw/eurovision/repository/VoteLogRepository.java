package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.VoteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteLogRepository extends JpaRepository<VoteLog, Long> {

    /** All vote logs for a specific show */
    List<VoteLog> findByShow_ShowId(Long showId);

    /** All jury votes for a specific show */
    List<VoteLog> findByShow_ShowIdAndJuryIsNotNull(Long showId);

    /** All citizen votes for a specific show */
    List<VoteLog> findByShow_ShowIdAndCitizenIsNotNull(Long showId);

    /** All vote logs for a specific song in a show */
    List<VoteLog> findByShow_ShowIdAndSong_SongId(Long showId, Long songId);

    /** Check if a jury member already voted in a show */
    boolean existsByShow_ShowIdAndJury_UserId(Long showId, Long juryId);

    /** Check if a citizen already voted in a show */
    boolean existsByShow_ShowIdAndCitizen_UserId(Long showId, Long citizenId);

    /** All votes by a specific jury member in a show (for retrieving their session) */
    List<VoteLog> findByShow_ShowIdAndJury_UserId(Long showId, Long juryId);

    /** All citizen votes grouped by country for score calculation */
    @Query("SELECT vl FROM VoteLog vl " +
            "WHERE vl.show.showId = :showId " +
            "AND vl.citizen IS NOT NULL " +
            "AND vl.song.songId = :songId")
    List<VoteLog> findCitizenVotesByShowAndSong(
            @Param("showId") Long showId,
            @Param("songId") Long songId);
}
