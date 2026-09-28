package com.fraudwatch.service;

import com.fraudwatch.entity.Rule;
import com.fraudwatch.entity.RuleType;
import com.fraudwatch.entity.Transaction;
import com.fraudwatch.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Decides whether a single rule fires for a (not yet saved) transaction. */
@Component
public class RuleEvaluator {

    private final TransactionRepository transactionRepository;

    public RuleEvaluator(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public boolean matches(Rule rule, Transaction txn) {
        if (rule.getType() == RuleType.AMOUNT_THRESHOLD) {
            return txn.getAmount().compareTo(rule.getThresholdAmount()) > 0;
        }
        if (rule.getType() == RuleType.VELOCITY) {
            Instant from = txn.getTimestamp().minusSeconds(rule.getWindowSeconds());
            long previous = transactionRepository.countBySenderAccountAndTimestampBetween(
                    txn.getSenderAccount(), from, txn.getTimestamp());
            // +1 counts the current transaction (not saved yet). Flag when total > N.
            return previous + 1 > rule.getMaxCount();
        }
        return false;
    }
}
