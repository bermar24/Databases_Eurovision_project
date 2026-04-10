package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * EERM Entity: Score
 * Attributes: score_id (PK), song_score
 *
 * The "Calculate" diamond in the EERM connects VoteLog -> Score.
 * In practice, score is the aggregated result for a Song.
 *
 * Relations:
 *   Score (1) -> Song (1)  — one score per song
 */
@Entity
@Table(name = "score")
@Getter @Setter @NoArgsConstructor
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id")
    private Long scoreId;

    @Column(name = "song_score", nullable = false)
    private Integer songScore = 0;

    /** Owning side of the 1:1 with Song */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false, unique = true)
    private Song song;
}
