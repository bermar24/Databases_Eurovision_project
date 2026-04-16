package com.dhbw.eurovision.service;

import com.dhbw.eurovision.dto.response.ScoreResponseDTO;
import com.dhbw.eurovision.entity.Score;
import com.dhbw.eurovision.entity.Show;
import com.dhbw.eurovision.entity.Song;
import com.dhbw.eurovision.entity.VoteLog;
import com.dhbw.eurovision.factory.ScoreFactory;
import com.dhbw.eurovision.repository.ScoreRepository;
import com.dhbw.eurovision.repository.ShowRepository;
import com.dhbw.eurovision.repository.SongRepository;
import com.dhbw.eurovision.repository.VoteLogRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Score — implements the EERM "Calculate" relationship.
 *
 * JURY scoring (Grand Final):
 *  *   Each jury submits a full ballot. Points are added directly as awarded.
 *  *   Total jury score for a song = sum of all jury VoteLog.points for that song.
 *  *
 *  * CITIZEN/TELEVOTE scoring:
 *  *   All citizens from the same country are pooled.
 *  *   Their raw votes for each song are summed per country.
 *  *   Each country's top 10 songs receive 12,10,8,7,6,5,4,3,2,1.
 *  *   Final citizen score for a song = sum of points received from all countries.
 *  *
 *  * SEMI-FINALS: 100% televote (citizen votes only, same aggregation).
 *  *
 *  * calculateShowScores(showId) runs both and stores the combined total per song.
 *  */
@Service
public class ScoreService {

    private static final int[] POINTS_SCALE = {12, 10, 8, 7, 6, 5, 4, 3, 2, 1};

    private final ScoreRepository scoreRepository;
    private final SongRepository songRepository;
    private final ShowRepository showRepository;
    private final VoteLogRepository voteLogRepository;
    private final ScoreFactory scoreFactory;

    public ScoreService(ScoreRepository scoreRepository,
                        SongRepository songRepository,
                        ShowRepository showRepository,
                        VoteLogRepository voteLogRepository,
                        ScoreFactory scoreFactory) {
        this.scoreRepository = scoreRepository;
        this.songRepository = songRepository;
        this.showRepository    = showRepository;
        this.voteLogRepository = voteLogRepository;
        this.scoreFactory = scoreFactory;
    }

    /** Full leaderboard — all scored songs. */
    public List<ScoreResponseDTO> getAllScores() {
        return scoreRepository.findAll().stream()
                .map(scoreFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    /** Score for one specific song. */
    public ScoreResponseDTO getScoreBySongId(Long songId) {
        return scoreFactory.toResponseDTO(
                scoreRepository.findBySong_SongId(songId)
                        .orElseThrow(() -> new RuntimeException(
                                "No score found for song: " + songId)));
    }

    /**
     * Calculate and persist scores for ALL songs in a show.
     *
     * Grand Final  → jury points (direct sum) + citizen points (country aggregation)
     * Semi-Finals  → citizen points only (country aggregation)
     */
    public List<ScoreResponseDTO> calculateShowScores(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found: " + showId));

        List<Song> songs = show.getSongs();
        if (songs.isEmpty()) {
            return Collections.emptyList();
        }

        boolean isGrandFinal = "Grand Final".equalsIgnoreCase(show.getShowName());

        // ── Jury score: direct sum of points per song ────────────────────
        Map<Long, Integer> juryScoreMap = new HashMap<>();
        if (isGrandFinal) {
            List<VoteLog> juryVotes = voteLogRepository
                    .findByShow_ShowIdAndJuryIsNotNull(showId);
            for (VoteLog vl : juryVotes) {
                juryScoreMap.merge(vl.getSong().getSongId(),
                        vl.getPoints() != null ? vl.getPoints() : 0, Integer::sum);
            }
        }

        // ── Citizen score: aggregate by country then award scale ─────────
        Map<Long, Integer> citizenScoreMap = new HashMap<>();
        List<VoteLog> citizenVotes = voteLogRepository
                .findByShow_ShowIdAndCitizenIsNotNull(showId);

        if (!citizenVotes.isEmpty()) {
            // Group raw votes: country → songId → total raw votes
            Map<String, Map<Long, Integer>> rawByCountry = new HashMap<>();
            for (VoteLog vl : citizenVotes) {
                String country = vl.getCitizen().getCountry().getCountryCode();
                Long   songId  = vl.getSong().getSongId();
                rawByCountry
                        .computeIfAbsent(country, k -> new HashMap<>())
                        .merge(songId, vl.getPoints() != null ? vl.getPoints() : 1, Integer::sum);
            }

            // For each country: rank songs → award 12,10,8,7,6,5,4,3,2,1 to top 10
            for (Map<Long, Integer> songRaw : rawByCountry.values()) {
                List<Map.Entry<Long, Integer>> ranked = new ArrayList<>(songRaw.entrySet());
                ranked.sort((a, b) -> b.getValue() - a.getValue()); // descending

                for (int i = 0; i < Math.min(10, ranked.size()); i++) {
                    Long songId = ranked.get(i).getKey();
                    citizenScoreMap.merge(songId, POINTS_SCALE[i], Integer::sum);
                }
            }
        }

        // ── Persist combined score per song ──────────────────────────────
        List<ScoreResponseDTO> results = new ArrayList<>();
        for (Song song : songs) {
            int jury    = juryScoreMap.getOrDefault(song.getSongId(), 0);
            int citizen = citizenScoreMap.getOrDefault(song.getSongId(), 0);
            int total   = jury + citizen;

            Score score = scoreRepository.findBySong(song).orElse(new Score());
            score.setSong(song);
            score.setSongScore(total);
            results.add(scoreFactory.toResponseDTO(scoreRepository.save(score)));
        }

        return results;
    }

    /** Recalculate one specific song (simple direct sum — for quick updates after a vote) */
    public ScoreResponseDTO calculateScoreForSong(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found: " + songId));
        int total = song.getVoteLogs().stream()
                .mapToInt(vl -> vl.getPoints() != null ? vl.getPoints() : 0)
                .sum();
        Score score = scoreRepository.findBySong(song).orElse(new Score());
        score.setSong(song);
        score.setSongScore(total);
        return scoreFactory.toResponseDTO(scoreRepository.save(score));
    }

    /** Recalculate every song across all shows */
    public List<ScoreResponseDTO> calculateAllScores() {
        return showRepository.findAll().stream()
                .flatMap(show -> calculateShowScores(show.getShowId()).stream())
                .collect(Collectors.toList());
    }
}