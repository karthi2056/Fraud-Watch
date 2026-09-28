package com.fraudwatch.repository;

import com.fraudwatch.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    long countBySenderAccountAndTimestampBetween(String senderAccount, Instant from, Instant to);
}
