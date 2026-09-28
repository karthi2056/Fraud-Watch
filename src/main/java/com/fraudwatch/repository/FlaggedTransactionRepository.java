package com.fraudwatch.repository;

import com.fraudwatch.entity.FlaggedTransaction;
import com.fraudwatch.entity.ReviewStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlaggedTransactionRepository extends JpaRepository<FlaggedTransaction, Long> {

    Page<FlaggedTransaction> findByReviewStatus(ReviewStatus status, Pageable pageable);

    /** Row lock so two reviewers cannot decide the same item at once. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FlaggedTransaction f where f.id = :id")
    Optional<FlaggedTransaction> findByIdForUpdate(@Param("id") Long id);

    @Query("select r.id as ruleId, r.name as ruleName, count(f.id) as flaggedCount " +
           "from FlaggedTransaction f join f.rules r group by r.id, r.name order by count(f.id) desc")
    List<RuleFlagCount> countFlaggedByRule();
}
