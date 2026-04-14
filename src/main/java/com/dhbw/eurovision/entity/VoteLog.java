package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * EERM Entity: Vote_Log
 * One row = one vote cast by a Jury member or Citizen for a Song.
 *
 * Eurovision points scale: 12, 10, 8, 7, 6, 5, 4, 3, 2, 1
 * Constraint: voter's country != song's country (no own-country vote).
 * Constraint: exactly one of jury_id / citizen_id must be non-null.
 */
@Entity
@Table(name = "vote_log")
@Getter @Setter @NoArgsConstructor
public class VoteLog {

    /** Valid Eurovision point values */
    public static final Set<Integer> VALID_POINTS = Set.of(12, 10, 8, 7, 6, 5, 4, 3, 2, 1);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_log_id")
    private Long voteLogId;

    /**
     * Points awarded — must be one of: 12, 10, 8, 7, 6, 5, 4, 3, 2, 1
     * Validated in VoteLogService before persisting.
     */
    @Column(name = "points", nullable = false)
    private Integer points;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    /** Set when this is a jury vote; null for citizen votes. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jury_id")
    private Jury jury;

    /** Set when this is a citizen/televote; null for jury votes. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id")
    private Citizen citizen;
}
