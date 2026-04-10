package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * EERM Entity: Vote_Log
 * Associative entity for the "Votes" M:N diamond between
 * (Jury or Citizen) and Song.
 *
 * One row = one vote cast.
 * Either jury_id OR citizen_id will be set, not both
 * (depending on whether it's a jury or public vote).
 */
@Entity
@Table(name = "vote_log")
@Getter @Setter @NoArgsConstructor
public class VoteLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_log_id")
    private Long voteLogId;

    // TODO: add a points/value field once your team agrees on voting rules
    // @Column(name = "points", nullable = false)
    // private Integer points;

    // --- Relationships ---

    /** The song this vote is for */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    /**
     * The Jury voter — null when this is a citizen vote.
     * Constraint: exactly one of jury / citizen must be non-null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jury_id")
    private Jury jury;

    /**
     * The Citizen voter — null when this is a jury vote.
     * Constraint: exactly one of jury / citizen must be non-null.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id")
    private Citizen citizen;
}
