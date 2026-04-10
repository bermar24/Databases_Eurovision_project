package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * EERM Entity: Country
 * Attributes: country_code (PK, underlined), country_name
 *
 * Relations:
 *   Country "Has" (1:M) -> User     (one country has many users)
 *   Country (1:M)       -> Song     (one country submits many songs)
 */
@Entity
@Table(name = "country")
@Getter @Setter @NoArgsConstructor
public class Country {

    /** PK — ISO 3166-1 alpha-2 code, e.g. "DE", "SE", "CH" */
    @Id
    @Column(name = "country_code", length = 3, nullable = false)
    private String countryCode;

    @Column(name = "country_name", nullable = false, unique = true)
    private String countryName;

    // --- Relationships ---

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Song> songs = new ArrayList<>();
}
