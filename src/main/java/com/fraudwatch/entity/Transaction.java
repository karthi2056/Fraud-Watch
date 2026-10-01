package com.fraudwatch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions",
        indexes = @Index(name = "idx_txn_sender_ts", columnList = "sender_account, txn_timestamp"))
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "senderAccount is required")
    @Pattern(regexp = "^\\d+$", message = "senderAccount must contain only numbers")
    @Column(name = "sender_account", nullable = false, length = 64)
    private String senderAccount;

    @NotBlank(message = "receiverAccount is required")
    @Pattern(regexp = "^\\d+$", message = "receiverAccount must contain only numbers")
    @Column(name = "receiver_account", nullable = false, length = 64)
    private String receiverAccount;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than 0")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** Optional in the request; defaults to now. */
    @Column(name = "txn_timestamp", nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    /** Not stored: names of the rules triggered, filled in on the create response. */
    @Transient
    private List<String> triggeredRules = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount(String receiverAccount) {
        this.receiverAccount = receiverAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public List<String> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(List<String> triggeredRules) {
        this.triggeredRules = triggeredRules;
    }
}
