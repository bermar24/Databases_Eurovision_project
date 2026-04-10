package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for User.
 * Extend with custom query methods as needed.
 * Spring Data JPA auto-generates: findAll, findById, save, deleteById, etc.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // TODO: add custom finders, e.g.:
    // List<User> findBy...(String param);
}
