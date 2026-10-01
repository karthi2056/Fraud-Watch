package com.fraudwatch.service;

import com.fraudwatch.entity.*;
import com.fraudwatch.exception.InvalidStateException;
import com.fraudwatch.exception.ResourceNotFoundException;
import com.fraudwatch.repository.FlaggedTransactionRepository;
import com.fraudwatch.repository.ReviewOutcomeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    private final FlaggedTransactionRepository flaggedRepository;
    private final ReviewOutcomeRepository outcomeRepository;
    private final com.fraudwatch.repository.UserRepository userRepository;

    public ReviewService(FlaggedTransactionRepository flaggedRepository,
                         ReviewOutcomeRepository outcomeRepository,
                         com.fraudwatch.repository.UserRepository userRepository) {
        this.flaggedRepository = flaggedRepository;
        this.outcomeRepository = outcomeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<FlaggedTransaction> list(ReviewStatus status, Pageable pageable) {
        return (status == null)
                ? flaggedRepository.findAll(pageable)
                : flaggedRepository.findByReviewStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public FlaggedTransaction get(Long id) {
        return flaggedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flagged transaction " + id + " not found"));
    }

    @Transactional
    public FlaggedTransaction review(Long flaggedId, ReviewOutcome req) {
        // Locked read: a concurrent second review waits, then sees the item is no longer pending.
        FlaggedTransaction flagged = flaggedRepository.findByIdForUpdate(flaggedId)
                .orElseThrow(() -> new ResourceNotFoundException("Flagged transaction " + flaggedId + " not found"));

        if (flagged.getReviewStatus() != ReviewStatus.PENDING_REVIEW) {
            throw new InvalidStateException("Flagged transaction " + flaggedId
                    + " has already been reviewed (" + flagged.getReviewStatus() + ")");
        }
        Transaction txn = flagged.getTransaction();
        if (txn.getStatus() != TransactionStatus.FLAGGED) {
            throw new InvalidStateException("Transaction " + txn.getId() + " is not in FLAGGED state");
        }

        // Fresh entity: the client only supplies decision / reviewer / comment.
        ReviewOutcome outcome = new ReviewOutcome();
        outcome.setFlaggedTransaction(flagged);
        outcome.setDecision(req.getDecision());
        outcome.setReviewer(req.getReviewer().trim());
        outcome.setComment(req.getComment());
        outcome.setReviewedAt(Instant.now());
        outcomeRepository.save(outcome);

        if (req.getDecision() == Decision.APPROVED) {
            flagged.setReviewStatus(ReviewStatus.APPROVED);
            txn.setStatus(TransactionStatus.COMPLETED);

            // Execute balance transfer now that Admin has approved
            userRepository.findByAccountNumber(txn.getSenderAccount()).ifPresent(senderUser -> {
                if (senderUser.getBalance().compareTo(txn.getAmount()) < 0) {
                    throw new InvalidStateException("Sender account (" + txn.getSenderAccount()
                            + ") has insufficient funds ($" + senderUser.getBalance() + ") to complete approved amount ($" + txn.getAmount() + ")");
                }
                senderUser.setBalance(senderUser.getBalance().subtract(txn.getAmount()));
                userRepository.save(senderUser);
            });
            userRepository.findByAccountNumber(txn.getReceiverAccount()).ifPresent(receiverUser -> {
                receiverUser.setBalance(receiverUser.getBalance().add(txn.getAmount()));
                userRepository.save(receiverUser);
            });
        } else {
            flagged.setReviewStatus(ReviewStatus.BLOCKED);
            txn.setStatus(TransactionStatus.BLOCKED); // never completes / touches balances
        }
        flagged.setReviewOutcome(outcome);
        return flaggedRepository.save(flagged);
    }

    /** Number of flagged transactions per triggered rule. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> dashboard() {
        return flaggedRepository.countFlaggedByRule().stream().map(r -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("ruleId", r.getRuleId());
            row.put("ruleName", r.getRuleName());
            row.put("flaggedCount", r.getFlaggedCount());
            return row;
        }).toList();
    }
}
