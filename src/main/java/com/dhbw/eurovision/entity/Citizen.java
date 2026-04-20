package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * EERM Entity: Citizen (subtype of User — public televote voter)
 *
 * phone_number is UNIQUE — one citizen account per phone number.
 * If a phone number already exists, the existing record is reused (no duplicates).
 * userId remains the PK (auto-generated); phone_number is a natural business key.
 */
@Entity
@Table(name = "citizen")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter @Setter @NoArgsConstructor
public class Citizen extends User {

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @OneToMany(mappedBy = "citizen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoteLog> voteLogs = new ArrayList<>();
}
