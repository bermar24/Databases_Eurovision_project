package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Country.
 * Extend with custom query methods as needed.
 * Spring Data JPA auto-generates: findAll, findById, save, deleteById, etc.
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, String> {

    // TODO: add custom finders, e.g.:
    // List<Country> findBy...(String param);
}
