package com.dhbw.eurovision.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * EERM Entity: Admin  (subtype of User)
 * Attributes: user_id (inherited PK/FK), admin_level
 *
 * DB table: "admin"
 *   -> admin.user_id FK -> user.user_id
 *
 * EERM "Manage" diamond: Admin (M) <-> (M) Show
 */
@Entity
@Table(name = "admin")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter @Setter @NoArgsConstructor
public class Admin extends User {

    @Column(name = "admin_level")
    private Integer adminLevel;

    /**
     * M:N — Admin manages Shows.
     * Join table: "admin_show" (admin_user_id, show_id)
     */
    @ManyToMany
    @JoinTable(
        name = "admin_show",
        joinColumns = @JoinColumn(name = "admin_user_id"),
        inverseJoinColumns = @JoinColumn(name = "show_id")
    )
    private List<Show> managedShows = new ArrayList<>();
}
