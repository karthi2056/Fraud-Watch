package com.fraudwatch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "rules", uniqueConstraints = @UniqueConstraint(name = "uk_rule_name", columnNames = "name"))
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RuleType type;

    /** AMOUNT_THRESHOLD: flag when amount is strictly greater than this. */
    @Column(name = "threshold_amount", precision = 19, scale = 2)
    private BigDecimal thresholdAmount;

    /** VELOCITY: flag when more than this many transactions occur within the window. */
    @Column(name = "max_count")
    private Integer maxCount;

    /** VELOCITY: window length in seconds. */
    @Column(name = "window_seconds")
    private Long windowSeconds;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = type;
    }

    public BigDecimal getThresholdAmount() {
        return thresholdAmount;
    }

    public void setThresholdAmount(BigDecimal thresholdAmount) {
        this.thresholdAmount = thresholdAmount;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Long getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(Long windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
