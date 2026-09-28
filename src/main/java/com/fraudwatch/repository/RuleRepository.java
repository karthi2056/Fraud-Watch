package com.fraudwatch.repository;

import com.fraudwatch.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findByActiveTrue();
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
