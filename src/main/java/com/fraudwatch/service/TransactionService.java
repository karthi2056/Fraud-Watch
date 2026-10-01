package com.fraudwatch.service;

import com.fraudwatch.entity.*;
import com.fraudwatch.exception.BadRequestException;
import com.fraudwatch.exception.InvalidStateException;
import com.fraudwatch.exception.ResourceNotFoundException;
import com.fraudwatch.repository.FlaggedTransactionRepository;
import com.fraudwatch.repository.RuleRepository;
import com.fraudwatch.repository.TransactionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RuleRepository ruleRepository;
    private final FlaggedTransactionRepository flaggedRepository;
    private final RuleEvaluator evaluator;
    private final com.fraudwatch.repository.UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, RuleRepository ruleRepository,
                              FlaggedTransactionRepository flaggedRepository, RuleEvaluator evaluator,
                              com.fraudwatch.repository.UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.ruleRepository = ruleRepository;
        this.flaggedRepository = flaggedRepository;
        this.evaluator = evaluator;
        this.userRepository = userRepository;
    }

    /** Records a transaction and evaluates all active rules BEFORE it is persisted. */
    @Transactional
    public Transaction create(Transaction req) {
        String sender = req.getSenderAccount() != null ? req.getSenderAccount().trim() : "";
        String receiver = req.getReceiverAccount() != null ? req.getReceiverAccount().trim() : "";
        if (!sender.matches("^\\d+$") || !receiver.matches("^\\d+$")) {
            throw new BadRequestException("Sender and receiver accounts must contain only numbers");
        }
        if (sender.equalsIgnoreCase(receiver)) {
            if ("1001".equals(sender)) {
                throw new BadRequestException("User 1 cannot send money to User 1");
            } else if ("1002".equals(sender)) {
                throw new BadRequestException("User 2 cannot send money to User 2");
            }
            throw new BadRequestException("Sender and receiver accounts must be different");
        }

        // Validate sender balance if registered user account
        userRepository.findByAccountNumber(sender).ifPresent(senderUser -> {
            if (senderUser.getBalance().compareTo(req.getAmount()) < 0) {
                throw new BadRequestException("Insufficient balance in account " + sender
                        + ". Current balance: $" + senderUser.getBalance() + ", Requested: $" + req.getAmount());
            }
        });

        // Build a fresh entity so a client can never set id / status directly.
        Transaction txn = new Transaction();
        txn.setSenderAccount(sender);
        txn.setReceiverAccount(receiver);
        txn.setAmount(req.getAmount());
        txn.setTimestamp(req.getTimestamp() != null ? req.getTimestamp() : Instant.now());

        // Collect ALL triggered rules (a transaction can be flagged by several at once).
        Set<Rule> triggered = new HashSet<>();
        for (Rule rule : ruleRepository.findByActiveTrue()) {
            if (evaluator.matches(rule, txn)) {
                triggered.add(rule);
            }
        }

        txn.setStatus(triggered.isEmpty() ? TransactionStatus.COMPLETED : TransactionStatus.FLAGGED);
        txn = transactionRepository.save(txn);

        if (!triggered.isEmpty()) {
            FlaggedTransaction flagged = new FlaggedTransaction();
            flagged.setTransaction(txn);
            flagged.setRules(triggered);
            flagged.setFlaggedAt(Instant.now());
            flagged.setReviewStatus(ReviewStatus.PENDING_REVIEW);
            flaggedRepository.save(flagged);

            List<String> names = new ArrayList<>();
            for (Rule r : triggered) names.add(r.getName());
            names.sort(String::compareTo);
            txn.setTriggeredRules(names);
        } else {
            // Passed all rules: execute balance transfer immediately
            userRepository.findByAccountNumber(sender).ifPresent(senderUser -> {
                senderUser.setBalance(senderUser.getBalance().subtract(req.getAmount()));
                userRepository.save(senderUser);
            });
            userRepository.findByAccountNumber(receiver).ifPresent(receiverUser -> {
                receiverUser.setBalance(receiverUser.getBalance().add(req.getAmount()));
                userRepository.save(receiverUser);
            });
        }
        return txn;
    }

    @Transactional(readOnly = true)
    public Transaction get(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Transaction> list() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /**
     * Completes (settles) a transaction. This is the single guard that keeps blocked or
     * still-under-review transactions from completing / affecting balances.
     */
    @Transactional
    public Transaction complete(Long id) {
        Transaction txn = get(id);
        if (txn.getStatus() == TransactionStatus.BLOCKED) {
            throw new InvalidStateException(
                    "Transaction " + id + " is BLOCKED and cannot be completed or affect balances");
        }
        if (txn.getStatus() == TransactionStatus.FLAGGED) {
            throw new InvalidStateException(
                    "Transaction " + id + " is awaiting manual review and cannot be completed yet");
        }
        return txn; // already COMPLETED: idempotent
    }
}
