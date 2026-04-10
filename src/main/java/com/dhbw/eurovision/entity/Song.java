package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * EERM Entity: Song
 * Attributes: song_id (PK), singer_name
 *
 * Relations:
 *   Song (M) -> Country (1)         — each song represents one country
 *   Song (1) -> Score (1)           — "Calculate" relationship
 *   Song (M) <-> Show (M)           — songs appear in shows
 *   Song (1) -> VoteLog (M)         — votes cast for this song
 */
@Entity
@Table(name = "song")
@Getter @Setter @NoArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "song_id")
    private Long songId;

    @Column(name = "singer_name", nullable = false)
    private String singerName;

    // TODO: add more fields your team agrees on, e.g.:
    // @Column(name = "song_title", nullable = false)
    // private String songTitle;
    //
    // @Column(name = "language")
    // private String language;

    // --- Relationships ---

    /** FK to Country */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    /** 1:1 — the calculated Score for this Song ("Calculate" in EERM) */
    @OneToOne(mappedBy = "song", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Score score;

    /** All individual vote entries for this song */
    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoteLog> voteLogs = new ArrayList<>();

    /** Shows in which this song participates (inverse side) */
    @ManyToMany(mappedBy = "songs")
    private List<Show> shows = new ArrayList<>();
}
