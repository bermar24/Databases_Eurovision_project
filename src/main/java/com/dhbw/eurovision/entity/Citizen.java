package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * EERM Entity: Citizen  (subtype of User — public voter)
 * Attributes: user_id (inherited PK/FK)
 *
 * DB table: "citizen"
 *   -> citizen.user_id FK -> user.user_id
 *
 * EERM "Votes" diamond: Jury/Citizen (M) -> VoteLog -> Song
 */
@Entity
@Table(name = "citizen")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter @Setter @NoArgsConstructor
public class Citizen extends User {

    // TODO: Add citizen-specific attributes if your team defines any
    // e.g. phone number for SMS voting verification

    /** Votes cast by this citizen */
    @OneToMany(mappedBy = "citizen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoteLog> voteLogs = new ArrayList<>();
}
