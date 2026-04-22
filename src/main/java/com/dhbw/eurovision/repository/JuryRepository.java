package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.Jury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Jury.
 * Extend with custom query methods as needed.
 * Spring Data JPA auto-generates: findAll, findById, save, deleteById, etc.
 */
@Repository
public interface JuryRepository extends JpaRepository<Jury, Long> {
}
