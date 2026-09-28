package com.fraudwatch.controller;

import com.fraudwatch.entity.FlaggedTransaction;
import com.fraudwatch.entity.ReviewOutcome;
import com.fraudwatch.entity.ReviewStatus;
import com.fraudwatch.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flagged")
public class FlaggedTransactionController {

    private final ReviewService service;

    public FlaggedTransactionController(ReviewService service) {
        this.service = service;
    }

    /** Review queue. Example: GET /api/flagged?status=PENDING_REVIEW&page=0&size=20 */
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "flaggedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort sort = Sort.by("desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
                "id".equals(sortBy) ? "id" : "flaggedAt");
        Page<FlaggedTransaction> result = service.list(status, PageRequest.of(Math.max(page, 0), safeSize, sort));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", result.getContent());
        body.put("page", result.getNumber());
        body.put("size", result.getSize());
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages", result.getTotalPages());
        return body;
    }

    @GetMapping("/{id}")
    public FlaggedTransaction get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/review")
    public FlaggedTransaction review(@PathVariable Long id, @Valid @RequestBody ReviewOutcome request) {
        return service.review(id, request);
    }
}
