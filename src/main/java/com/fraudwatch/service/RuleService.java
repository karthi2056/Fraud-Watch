package com.fraudwatch.service;

import com.fraudwatch.entity.Rule;
import com.fraudwatch.entity.RuleType;
import com.fraudwatch.exception.BadRequestException;
import com.fraudwatch.exception.InvalidStateException;
import com.fraudwatch.exception.ResourceNotFoundException;
import com.fraudwatch.repository.RuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RuleService {

    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Transactional
    public Rule create(Rule req) {
        validate(req);
        if (ruleRepository.existsByName(req.getName().trim())) {
            throw new InvalidStateException("A rule named '" + req.getName().trim() + "' already exists");
        }
        return ruleRepository.save(apply(new Rule(), req));
    }

    @Transactional
    public Rule update(Long id, Rule req) {
        validate(req);
        Rule rule = get(id);
        if (ruleRepository.existsByNameAndIdNot(req.getName().trim(), id)) {
            throw new InvalidStateException("A rule named '" + req.getName().trim() + "' already exists");
        }
        return ruleRepository.save(apply(rule, req));
    }

    @Transactional(readOnly = true)
    public List<Rule> list() {
        return ruleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Rule get(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule " + id + " not found"));
    }

    /** Type-specific validation: rejected in the service layer, not left to the database. */
    private void validate(Rule req) {
        if (req.getType() == RuleType.AMOUNT_THRESHOLD) {
            if (req.getThresholdAmount() == null || req.getThresholdAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("AMOUNT_THRESHOLD rule requires thresholdAmount > 0");
            }
        } else if (req.getType() == RuleType.VELOCITY) {
            if (req.getMaxCount() == null || req.getMaxCount() < 1) {
                throw new BadRequestException("VELOCITY rule requires maxCount >= 1");
            }
            if (req.getWindowSeconds() == null || req.getWindowSeconds() < 1) {
                throw new BadRequestException("VELOCITY rule requires windowSeconds >= 1");
            }
        }
    }

    private Rule apply(Rule rule, Rule req) {
        rule.setName(req.getName().trim());
        rule.setType(req.getType());
        rule.setThresholdAmount(req.getType() == RuleType.AMOUNT_THRESHOLD ? req.getThresholdAmount() : null);
        rule.setMaxCount(req.getType() == RuleType.VELOCITY ? req.getMaxCount() : null);
        rule.setWindowSeconds(req.getType() == RuleType.VELOCITY ? req.getWindowSeconds() : null);
        rule.setActive(req.isActive());
        return rule;
    }
}
