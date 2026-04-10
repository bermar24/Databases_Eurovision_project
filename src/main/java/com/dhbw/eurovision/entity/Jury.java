package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * EERM Entity: Jury  (subtype of User)
 * Attributes: user_id (inherited PK/FK), professional_bg
 *
 * DB table: "jury"
 *   -> jury.user_id FK -> user.user_id
 *
 * EERM "Votes" diamond: Jury/Citizen (M) -> VoteLog -> Song
 */
@Entity
@Table(name = "jury")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter @Setter @NoArgsConstructor
public class Jury extends User {

    @Column(name = "professional_bg")
    private String professionalBg;

    /** Votes cast by this jury member */
    @OneToMany(mappedBy = "jury", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoteLog> voteLogs = new ArrayList<>();
}
