package com.fraudwatch.controller;

import com.fraudwatch.entity.Rule;
import com.fraudwatch.service.RuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleService service;

    public RuleController(RuleService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Rule create(@Valid @RequestBody Rule request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Rule update(@PathVariable Long id, @Valid @RequestBody Rule request) {
        return service.update(id, request);
    }

    @GetMapping
    public List<Rule> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Rule get(@PathVariable Long id) {
        return service.get(id);
    }
}
