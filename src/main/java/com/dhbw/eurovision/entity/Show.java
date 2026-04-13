package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * EERM Entity: Show
 * e.g. Semi-Final 1, Semi-Final 2, Grand Final
 *
 * Relations:
 *   Show (M) <-> Admin (M)   — "Manage" diamond (admin_show join table)
 *   Show (M) <-> Song  (M)   — songs that compete in this show (show_song join table)
 */
@Entity
@Table(name = "show")
@Getter @Setter @NoArgsConstructor
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "show_id")
    private Long showId;

    @Column(name = "show_name", nullable = false)
    private String showName;

    // TODO: add show fields once team agrees, e.g.:
    // @Column(name = "show_name", nullable = false)
    // private String showName;
    //
    // @Column(name = "show_date")
    // private LocalDate showDate;
    //
    // @Enumerated(EnumType.STRING)
    // @Column(name = "show_type")
    // private ShowType showType;  // SEMI_FINAL_1, SEMI_FINAL_2, GRAND_FINAL

    // --- Relationships ---

    /**
     * M:N — Songs that compete in this show.
     * Join table: "show_song"
     */
    @ManyToMany
    @JoinTable(
        name = "show_song",
        joinColumns = @JoinColumn(name = "show_id"),
        inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<Song> songs = new ArrayList<>();

    /** M:N inverse side — Admins that manage this show */
    @ManyToMany(mappedBy = "managedShows")
    private List<Admin> admins = new ArrayList<>();
}
