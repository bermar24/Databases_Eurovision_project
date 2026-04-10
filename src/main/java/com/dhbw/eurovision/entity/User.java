package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * EERM Entity: User  (base class)
 * Attributes: user_id (PK)
 *
 * Inheritance strategy: JOINED
 *   -> table "user" holds common columns
 *   -> subtables "jury", "citizen", "admin" each share user_id as FK/PK
 *
 * EERM "Is a" triangle: User -> Jury | Citizen | Admin
 * EERM "Has" diamond:   Country (1) -> (M) User
 */
@Entity
@Table(name = "user")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // TODO: add shared fields once agreed with team, e.g.:
    // @Column(name = "username", nullable = false, unique = true)
    // private String username;
    //
    // @Column(name = "email", nullable = false, unique = true)
    // private String email;

    /** FK to Country — the "Has" relationship from EERM */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;
}
