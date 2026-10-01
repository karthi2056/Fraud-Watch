package com.fraudwatch.service;

import com.fraudwatch.entity.Role;
import com.fraudwatch.entity.User;
import com.fraudwatch.exception.BadRequestException;
import com.fraudwatch.exception.ResourceNotFoundException;
import com.fraudwatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> list() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User '" + username + "' not found"));
    }

    @Transactional(readOnly = true)
    public User getByAccountNumber(String accountNumber) {
        return userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account '" + accountNumber + "' not found"));
    }

    @Transactional
    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("Username '" + user.getUsername() + "' is already taken");
        }
        if (userRepository.existsByAccountNumber(user.getAccountNumber())) {
            throw new BadRequestException("Account number '" + user.getAccountNumber() + "' is already in use");
        }
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }
        return userRepository.save(user);
    }

    @Transactional
    public void resetDemoUsers() {
        User u1 = userRepository.findByUsername("user1").orElse(new User("user1", "User 1", "1001", Role.USER, new BigDecimal("50000.00")));
        u1.setBalance(new BigDecimal("50000.00"));
        u1.setAccountNumber("1001");
        u1.setFullName("User 1");
        u1.setRole(Role.USER);
        userRepository.save(u1);

        User u2 = userRepository.findByUsername("user2").orElse(new User("user2", "User 2", "1002", Role.USER, new BigDecimal("10000.00")));
        u2.setBalance(new BigDecimal("10000.00"));
        u2.setAccountNumber("1002");
        u2.setFullName("User 2");
        u2.setRole(Role.USER);
        userRepository.save(u2);

        User admin = userRepository.findByUsername("admin").orElse(new User("admin", "Admin", "9999", Role.ADMIN, BigDecimal.ZERO));
        admin.setFullName("Admin");
        admin.setAccountNumber("9999");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }
}
