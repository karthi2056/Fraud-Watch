package com.fraudwatch;

import com.fraudwatch.entity.Role;
import com.fraudwatch.entity.Rule;
import com.fraudwatch.entity.RuleType;
import com.fraudwatch.entity.User;
import com.fraudwatch.repository.RuleRepository;
import com.fraudwatch.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class FraudWatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudWatchApplication.class, args);
    }

    /** Seeds default rules and users (User 1, User 2, Admin) on startup. */
    @Bean
    CommandLineRunner seedData(RuleRepository ruleRepo, UserRepository userRepo) {
        return args -> {
            // Seed or update default rules
            if (ruleRepo.count() == 0) {
                Rule amount = new Rule();
                amount.setName("High Amount");
                amount.setType(RuleType.AMOUNT_THRESHOLD);
                amount.setThresholdAmount(new BigDecimal("10000.00"));
                ruleRepo.save(amount);

                Rule velocity = new Rule();
                velocity.setName("Rapid Transfers");
                velocity.setType(RuleType.VELOCITY);
                velocity.setMaxCount(3);
                velocity.setWindowSeconds(60L);
                ruleRepo.save(velocity);
            } else {
                ruleRepo.findAll().stream()
                        .filter(r -> "High Amount".equalsIgnoreCase(r.getName()) && r.getThresholdAmount().compareTo(new BigDecimal("10000.00")) > 0)
                        .forEach(r -> {
                            r.setThresholdAmount(new BigDecimal("10000.00"));
                            ruleRepo.save(r);
                        });
            }

            // Seed User 1, User 2, and Admin
            if (!userRepo.existsByUsername("user1")) {
                userRepo.save(new User("user1", "User 1", "1001", Role.USER, new BigDecimal("50000.00")));
            }
            if (!userRepo.existsByUsername("user2")) {
                userRepo.save(new User("user2", "User 2", "1002", Role.USER, new BigDecimal("10000.00")));
            }
            if (!userRepo.existsByUsername("admin")) {
                userRepo.save(new User("admin", "Admin", "9999", Role.ADMIN, BigDecimal.ZERO));
            }
        };
    }
}

