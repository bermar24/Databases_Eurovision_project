package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Admin.
 * Extend with custom query methods as needed.
 * Spring Data JPA auto-generates: findAll, findById, save, deleteById, etc.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // TODO: add custom finders, e.g.:
    // List<Admin> findBy...(String param);
}
