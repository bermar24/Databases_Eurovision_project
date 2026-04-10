package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Citizen.
 * Extend with custom query methods as needed.
 * Spring Data JPA auto-generates: findAll, findById, save, deleteById, etc.
 */
@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    // TODO: add custom finders, e.g.:
    // List<Citizen> findBy...(String param);
}
