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
 * Score calculation — implements the EERM "Calculate" relationship.
 *
 * One Score row per (song, show) pair.
 * This means a song that appears in both a semi-final and the Grand Final
 * has two independent Score rows and its scores are never overwritten.
 *
 * ── JURY scoring (Grand Final only) ─────────────────────────────────────────
 *   National aggregation:
 *   1. Group jury VoteLogs by voter's country.
 *   2. Per country: sum raw points per song → national ranking.
 *   3. Top 10 songs in each country's ranking get 12,10,8,7,6,5,4,3,2,1.
 *   4. Jury score = sum of scale-points across all countries.
 *   Each country contributes exactly one set of points regardless of juror count.
 *
 * ── CITIZEN / TELEVOTE scoring (all shows) ──────────────────────────────────
 *   Same aggregation as jury but using citizen's country.
 *
 * ── Semi-Finals ──────────────────────────────────────────────────────────────
 *   100% televote only — jury block skipped.
 *
 * ── Grand Final ──────────────────────────────────────────────────────────────
 *   jury score + citizen score summed together.
 */
@Service
public class ScoreService {

    private static final int[]  POINTS_SCALE = {12, 10, 8, 7, 6, 5, 4, 3, 2, 1};
    private static final String GRAND_FINAL  = "grand final";

    private final ScoreRepository   scoreRepository;
    private final SongRepository    songRepository;
    private final ShowRepository    showRepository;
    private final VoteLogRepository voteLogRepository;
    private final ScoreFactory      scoreFactory;

    public ScoreService(ScoreRepository scoreRepository,
                        SongRepository songRepository,
                        ShowRepository showRepository,
                        VoteLogRepository voteLogRepository,
                        ScoreFactory scoreFactory) {
        this.scoreRepository   = scoreRepository;
        this.songRepository    = songRepository;
        this.showRepository    = showRepository;
        this.voteLogRepository = voteLogRepository;
        this.scoreFactory      = scoreFactory;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** All scores across all shows — full leaderboard. */
    public List<ScoreResponseDTO> getAllScores() {
        return scoreRepository.findAll().stream()
                .map(scoreFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    /** All scores for a specific show — show leaderboard. */
    public List<ScoreResponseDTO> getScoresByShow(Long showId) {
        return scoreRepository.findByShow_ShowId(showId).stream()
                .map(scoreFactory::toResponseDTO)
                .sorted(Comparator.comparingInt(ScoreResponseDTO::getSongScore).reversed())
                .collect(Collectors.toList());
    }

    /** All scores for a specific song across all shows it appeared in. */
    public List<ScoreResponseDTO> getScoresBySong(Long songId) {
        return scoreRepository.findBySong_SongId(songId).stream()
                .map(scoreFactory::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Full score calculation for all songs in a show.
     * Grand Final : jury aggregation + citizen aggregation
     * Semi-Finals : citizen aggregation only
     * Results are upserted into Score table as (song, show) pairs.
     */
    public List<ScoreResponseDTO> calculateShowScores(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found: " + showId));

        List<Song> songs = show.getSongs();
        if (songs.isEmpty()) return Collections.emptyList();

        boolean isGrandFinal = show.getShowName() != null &&
                show.getShowName().trim().equalsIgnoreCase(GRAND_FINAL);

        // ── Jury score (Grand Final only) ─────────────────────────────────
        Map<Long, Integer> juryScoreMap = isGrandFinal
                ? aggregateByCountry(
                voteLogRepository.findByShow_ShowIdAndJuryIsNotNull(showId), true)
                : Collections.emptyMap();

        // ── Citizen score (all shows) ─────────────────────────────────────
        Map<Long, Integer> citizenScoreMap = aggregateByCountry(
                voteLogRepository.findByShow_ShowIdAndCitizenIsNotNull(showId), false);

        // ── Upsert one Score row per (song, show) ─────────────────────────
        List<ScoreResponseDTO> results = new ArrayList<>();
        for (Song song : songs) {
            int jury    = juryScoreMap.getOrDefault(song.getSongId(), 0);
            int citizen = citizenScoreMap.getOrDefault(song.getSongId(), 0);

            // Find existing (song, show) score or create new one
            Score score = scoreRepository.findBySongAndShow(song, show)
                    .orElse(new Score());
            score.setSong(song);
            score.setShow(show);
            score.setSongScore(jury + citizen);

            results.add(scoreFactory.toResponseDTO(scoreRepository.save(score)));
        }

        return results;
    }

    /**
     * Quick recalculation for a single song in a specific show.
     * Uses direct point sum (not country aggregation) — for fast post-vote updates.
     */
    public ScoreResponseDTO calculateScoreForSong(Long songId, Long showId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found: " + songId));
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found: " + showId));

        int total = song.getVoteLogs().stream()
                .filter(vl -> vl.getShow() != null &&
                        vl.getShow().getShowId().equals(showId))
                .mapToInt(vl -> vl.getPoints() != null ? vl.getPoints() : 0)
                .sum();

        Score score = scoreRepository.findBySongAndShow(song, show).orElse(new Score());
        score.setSong(song);
        score.setShow(show);
        score.setSongScore(total);
        return scoreFactory.toResponseDTO(scoreRepository.save(score));
    }

    /** Recalculate every show — convenience method for full refresh. */
    public List<ScoreResponseDTO> calculateAllScores() {
        return showRepository.findAll().stream()
                .flatMap(show -> calculateShowScores(show.getShowId()).stream())
                .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * National aggregation — identical for jury and citizen.
     * Groups votes by voter's country, ranks songs per country,
     * awards 12,10,8,7,6,5,4,3,2,1 to each country's top 10.
     */
    private Map<Long, Integer> aggregateByCountry(
            List<VoteLog> votes, boolean isJury) {

        if (votes.isEmpty()) return Collections.emptyMap();

        // Step 1 — group raw points: country → songId → sum
        Map<String, Map<Long, Integer>> rawByCountry = new HashMap<>();
        for (VoteLog vl : votes) {
            String country;
            if (isJury) {
                if (vl.getJury() == null || vl.getJury().getCountry() == null) continue;
                country = vl.getJury().getCountry().getCountryCode();
            } else {
                if (vl.getCitizen() == null || vl.getCitizen().getCountry() == null) continue;
                country = vl.getCitizen().getCountry().getCountryCode();
            }
            Long songId = vl.getSong().getSongId();
            int  pts    = vl.getPoints() != null ? vl.getPoints() : 0;
            rawByCountry.computeIfAbsent(country, k -> new HashMap<>())
                    .merge(songId, pts, Integer::sum);
        }

        // Step 2 — per country: sort descending → award scale to top 10
        Map<Long, Integer> scoreMap = new HashMap<>();
        for (Map<Long, Integer> countryVotes : rawByCountry.values()) {
            List<Map.Entry<Long, Integer>> ranked = new ArrayList<>(countryVotes.entrySet());
            ranked.sort((a, b) -> b.getValue() - a.getValue());
            for (int i = 0; i < Math.min(10, ranked.size()); i++) {
                scoreMap.merge(ranked.get(i).getKey(), POINTS_SCALE[i], Integer::sum);
            }
        }

        return scoreMap;
    }
}