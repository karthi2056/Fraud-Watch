package com.fraudwatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(name = "review_outcomes",
        uniqueConstraints = @UniqueConstraint(name = "uk_outcome_flagged", columnNames = "flagged_transaction_id"))
public class ReviewOutcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(optional = false)
    @JoinColumn(name = "flagged_transaction_id", nullable = false)
    private FlaggedTransaction flaggedTransaction;

    @NotNull(message = "decision is required (APPROVED or BLOCKED)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Decision decision;

    @NotBlank(message = "reviewer is required")
    @Size(max = 100, message = "reviewer must be at most 100 characters")
    @Column(nullable = false, length = 100)
    private String reviewer;

    @Size(max = 500, message = "comment must be at most 500 characters")
    @Column(length = 500)
    private String comment;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FlaggedTransaction getFlaggedTransaction() {
        return flaggedTransaction;
    }

    public void setFlaggedTransaction(FlaggedTransaction flaggedTransaction) {
        this.flaggedTransaction = flaggedTransaction;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
