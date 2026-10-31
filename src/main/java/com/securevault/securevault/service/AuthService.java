package com.securevault.securevault.service;

import com.securevault.securevault.dto.AuthResponse;
import com.securevault.securevault.dto.LoginRequest;
import com.securevault.securevault.dto.RegisterRequest;
import com.securevault.securevault.entity.Account;
import com.securevault.securevault.entity.User;
import com.securevault.securevault.repository.AccountRepository;
import com.securevault.securevault.repository.UserRepository;
import com.securevault.securevault.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.CUSTOMER)
                .build();

        user = userRepository.save(user);

        Account account = Account.builder()
                .user(user)
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .build();

        accountRepository.save(account);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    private String generateAccountNumber() {
        return "SV" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}