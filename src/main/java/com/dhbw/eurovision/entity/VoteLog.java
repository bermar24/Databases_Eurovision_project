package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * EERM Entity: Vote_Log
 *
 * One row = one point assignment within a voting session.
 * A full ballot = 10 rows (one per point value) sharing the same vote_session_id.
 *
 * Eurovision points scale: 12, 10, 8, 7, 6, 5, 4, 3, 2, 1
 *
 * Constraints (enforced in VoteLogService):
 *   - Exactly one of jury_id / citizen_id is non-null.
 *   - Voter's country != song's country (no own-country vote).
 *   - All 10 point values used exactly once per session.
 *   - Voter may only have one session per show.
 *   - Voter's country must be eligible for the selected show.
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

    /** Points awarded — must be one of: 12, 10, 8, 7, 6, 5, 4, 3, 2, 1 */
    @Column(name = "points", nullable = false)
    private Integer points;

    /**
     * Groups the 10 rows of one voting session together.
     * UUID generated per ballot submission in VoteLogService.
     */
    @Column(name = "vote_session_id", nullable = false, length = 36)
    private String voteSessionId;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    /** The show this vote belongs to — used for eligibility + one-vote-per-show check */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    /** Set when this is a jury vote; null for citizen votes. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jury_id")
    private Jury jury;

    /** Set when this is a citizen/televote; null for jury votes. */
      @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id")
    private Citizen citizen;
}
