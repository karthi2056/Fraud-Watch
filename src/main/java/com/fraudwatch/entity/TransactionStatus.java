package com.fraudwatch.entity;

public enum TransactionStatus {
    COMPLETED,   // passed all rules (or approved by a reviewer)
    FLAGGED,     // held for manual review; not completed
    BLOCKED      // rejected by reviewer; can never complete
}
