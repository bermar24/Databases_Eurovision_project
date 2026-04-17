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
 * ── JURY scoring (Grand Final only) ─────────────────────────────────────────
 *   Mirrors the real Eurovision national jury aggregation:
 *   1. Collect all jury VoteLogs for the show.
 *   2. Group by voter's country (each country has 1–5 jurors).
 *   3. For each country: sum each juror's raw points per song
 *      → this produces a national ranking (higher total = higher rank).
 *   4. Each country's top 10 songs receive the scale: 12,10,8,7,6,5,4,3,2,1.
 *   5. Jury score per song = sum of scale-points awarded across all countries.
 *
 *   Result: regardless of how many jurors a country has, each country
 *   contributes exactly one set of 12,10,8,...,1 to the final scores —
 *   faithful to the real Eurovision jury aggregation model.
 *
 * ── CITIZEN / TELEVOTE scoring (all shows) ──────────────────────────────────
 *   Same aggregation as jury but uses citizen voter's country:
 *   1. Group citizen VoteLogs by voter's country.
 *   2. Sum raw points per song per country → national ranking.
 *   3. Each country awards 12,10,8,7,6,5,4,3,2,1 to its top 10.
 *   4. Citizen score = sum across all countries.
 *
 * ── Semi-Finals ──────────────────────────────────────────────────────────────
 *   100% televote — jury scoring block is skipped entirely.
 *
 * ── Grand Final ──────────────────────────────────────────────────────────────
 *   50% jury + 50% televote — both blocks run, results are summed.
 */
@Service
public class ScoreService {

    private static final int[] POINTS_SCALE = {12, 10, 8, 7, 6, 5, 4, 3, 2, 1};
    private static final String GRAND_FINAL   = "grand final";

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
     * Full score calculation for all songs in a show.
     *
     * Grand Final : jury aggregation + citizen aggregation
     * Semi-Finals : citizen aggregation only
     */
    public List<ScoreResponseDTO> calculateShowScores(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found: " + showId));

        List<Song> songs = show.getSongs();
        if (songs.isEmpty()) return Collections.emptyList();

        boolean isGrandFinal = show.getShowName() != null &&
                show.getShowName().trim().equalsIgnoreCase(GRAND_FINAL);

        // ── Jury score: national aggregation (Grand Final only) ───────────
        Map<Long, Integer> juryScoreMap = new HashMap<>();
        if (isGrandFinal) {
            List<VoteLog> juryVotes = voteLogRepository
                    .findByShow_ShowIdAndJuryIsNotNull(showId);
            juryScoreMap = aggregateByCountry(juryVotes, true);
        }

        // ── Citizen score: national aggregation (all shows) ───────────────
        List<VoteLog> citizenVotes = voteLogRepository
                .findByShow_ShowIdAndCitizenIsNotNull(showId);
        Map<Long, Integer> citizenScoreMap = aggregateByCountry(citizenVotes, false);

        // ── Persist combined score per song ───────────────────────────────
        List<ScoreResponseDTO> results = new ArrayList<>();
        for (Song song : songs) {
            int jury    = juryScoreMap.getOrDefault(song.getSongId(), 0);
            int citizen = citizenScoreMap.getOrDefault(song.getSongId(), 0);

            Score score = scoreRepository.findBySong(song).orElse(new Score());
            score.setSong(song);
            score.setSongScore(jury + citizen);
            results.add(scoreFactory.toResponseDTO(scoreRepository.save(score)));
        }

        return results;
    }

    /** Quick single-song recalculation — simple direct point sum. */
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

    /** Recalculate every song across all shows. */
    public List<ScoreResponseDTO> calculateAllScores() {
        return showRepository.findAll().stream()
                .flatMap(show -> calculateShowScores(show.getShowId()).stream())
                .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**

     * Algorithm:
     *   1. Group VoteLogs by voter's country code.
     *   2. For each country: sum raw points per song across all voters
     *      from that country → produces a national ranking.
     *   3. Sort songs by national total descending.
     *   4. Top 10 songs receive 12,10,8,7,6,5,4,3,2,1 from that country.
     *   5. Accumulate those scale-points per song across all countries.
     *
     * For jury:   one country's 5 jurors each submit a ballot (10 rows).
     *             Their raw points per song are summed → national ranking.
     *             Result: each country contributes one set of 12,10,8,...,1
     *             regardless of how many jurors voted.
     *
     * For citizen: same logic — each country's citizens pool their votes
     *             into one national ranking.
     *
     * @param votes    list of VoteLog rows (all for the same show)
     * @param isJury   true = read voter country from jury FK,
     *                 false = read from citizen FK
     * @return map of songId → awarded scale-points total
     */
    private Map<Long, Integer> aggregateByCountry(
            List<VoteLog> votes, boolean isJury) {

        if (votes.isEmpty()) return Collections.emptyMap();

        // Step 1 — group raw points: country → songId → sum of raw points
        Map<String, Map<Long, Integer>> rawByCountry = new HashMap<>();

        for (VoteLog vl : votes) {
            // Resolve voter's country
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

            rawByCountry
                    .computeIfAbsent(country, k -> new HashMap<>())
                    .merge(songId, pts, Integer::sum);
        }

        // Step 2 — for each country: rank songs → award scale points
        Map<Long, Integer> scoreMap = new HashMap<>();

        for (Map.Entry<String, Map<Long, Integer>> entry : rawByCountry.entrySet()) {
            // Sort songs by their raw national total, descending
            List<Map.Entry<Long, Integer>> ranked =
                    new ArrayList<>(entry.getValue().entrySet());
            ranked.sort((a, b) -> b.getValue() - a.getValue());

            // Award 12,10,8,7,6,5,4,3,2,1 to top 10
            for (int i = 0; i < Math.min(10, ranked.size()); i++) {
                Long songId = ranked.get(i).getKey();
                scoreMap.merge(songId, POINTS_SCALE[i], Integer::sum);
            }
        }

        return scoreMap;
    }
}