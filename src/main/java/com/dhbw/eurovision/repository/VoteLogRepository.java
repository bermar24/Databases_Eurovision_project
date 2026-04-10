package com.dhbw.eurovision.repository;

import com.dhbw.eurovision.entity.VoteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for VoteLog.
 * Extend with custom query methods as needed.
 * Spring Data JPA auto-generates: findAll, findById, save, deleteById, etc.
 */
@Repository
public interface VoteLogRepository extends JpaRepository<VoteLog, Long> {

    // TODO: add custom finders, e.g.:
    // List<VoteLog> findBy...(String param);
}
