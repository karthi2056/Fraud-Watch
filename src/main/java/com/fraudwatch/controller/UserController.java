package com.fraudwatch.controller;

import com.fraudwatch.entity.User;
import com.fraudwatch.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> list() {
        return userService.list();
    }

    @GetMapping("/{username}")
    public User get(@PathVariable String username) {
        return userService.getByUsername(username);
    }

    @GetMapping("/by-account/{accountNumber}")
    public User getByAccount(@PathVariable String accountNumber) {
        return userService.getByAccountNumber(accountNumber);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PostMapping("/reset")
    public Map<String, String> resetDemoUsers() {
        userService.resetDemoUsers();
        return Map.of("message", "Demo users (User 1, User 2, Admin) successfully initialized/reset");
    }
}
