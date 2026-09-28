package com.fraudwatch.controller;

import com.fraudwatch.service.ReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ReviewService service;

    public DashboardController(ReviewService service) {
        this.service = service;
    }

    /** Number of flagged transactions per triggered rule. */
    @GetMapping("/flagged-by-rule")
    public List<Map<String, Object>> flaggedByRule() {
        return service.dashboard();
    }
}
