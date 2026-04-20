package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * EERM Entity: Score
 * Attributes: score_id (PK), song_score
 *
 * A Score belongs to a Song IN THE CONTEXT OF a specific Show.
 * One row per (song, show) pair — enforced by UNIQUE(song_id, show_id).
 *
 * This reflects the real Eurovision model:
 *   - A song can appear in Semi-Final 1 AND the Grand Final.
 *   - Its score in each show is independent and must not overwrite the other.
 *
 * Relations:
 *   Score (M) → Song (1)  — many scores per song (one per show it appears in)
 *   Score (M) → Show (1)  — many scores per show (one per song in that show)
 *   VoteLog → Score       — the "Calculate" diamond: votes are aggregated into score
 */
@Entity
@Table(
        name = "score",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_score_song_show",
                columnNames = {"song_id", "show_id"}
        )
)
@Getter @Setter @NoArgsConstructor
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id")
    private Long scoreId;

    @Column(name = "song_score", nullable = false)
    private Integer songScore = 0;

    /** The song this score belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    /**
     * The show this score belongs to.
     * Together with song_id forms the natural business key.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;
}