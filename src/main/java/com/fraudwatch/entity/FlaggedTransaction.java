package com.fraudwatch.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "flagged_transactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_flagged_txn", columnNames = "transaction_id"))
public class FlaggedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** One transaction is flagged at most once, however many rules fire. */
    @OneToOne(optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    /** All rules that triggered for this transaction. */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "flagged_transaction_rules",
            joinColumns = @JoinColumn(name = "flagged_transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "rule_id"))
    private Set<Rule> rules = new HashSet<>();

    @Column(name = "flagged_at", nullable = false)
    private Instant flaggedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 20)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING_REVIEW;

    @OneToOne(mappedBy = "flaggedTransaction")
    private ReviewOutcome reviewOutcome;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Set<Rule> getRules() {
        return rules;
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }

    public Instant getFlaggedAt() {
        return flaggedAt;
    }

    public void setFlaggedAt(Instant flaggedAt) {
        this.flaggedAt = flaggedAt;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public ReviewOutcome getReviewOutcome() {
        return reviewOutcome;
    }

    public void setReviewOutcome(ReviewOutcome reviewOutcome) {
        this.reviewOutcome = reviewOutcome;
    }
}
