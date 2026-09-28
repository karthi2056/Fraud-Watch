package com.fraudwatch.controller;

import com.fraudwatch.entity.Transaction;
import com.fraudwatch.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@Valid @RequestBody Transaction request) {
        return service.create(request);
    }

    @GetMapping
    public List<Transaction> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public Transaction get(@PathVariable Long id) {
        return service.get(id);
    }

    /** Settlement attempt: rejected (409) if the transaction is BLOCKED or pending review. */
    @PostMapping("/{id}/complete")
    public Transaction complete(@PathVariable Long id) {
        return service.complete(id);
    }
}
