package com.fraudwatch;

import com.fraudwatch.entity.Rule;
import com.fraudwatch.entity.RuleType;
import com.fraudwatch.repository.RuleRepository;
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

    /** Seeds two default rules on first start. */
    @Bean
    CommandLineRunner seedRules(RuleRepository repo) {
        return args -> {
            if (repo.count() > 0) return;

            Rule amount = new Rule();
            amount.setName("High Amount");
            amount.setType(RuleType.AMOUNT_THRESHOLD);
            amount.setThresholdAmount(new BigDecimal("10000.00"));
            repo.save(amount);

            Rule velocity = new Rule();
            velocity.setName("Rapid Transfers");
            velocity.setType(RuleType.VELOCITY);
            velocity.setMaxCount(3);
            velocity.setWindowSeconds(60L);
            repo.save(velocity);
        };
    }
}
