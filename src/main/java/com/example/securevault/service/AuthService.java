package com.example.securevault.service;

import com.example.securevault.dto.AuthResponse;
import com.example.securevault.dto.LoginRequest;
import com.example.securevault.dto.RegisterRequest;
import com.example.securevault.model.User;
import com.example.securevault.repository.UserRepository;
import com.example.securevault.security.LoginAttemptService;
import com.example.securevault.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();

        if (loginAttemptService.isLocked(email)) {
            long remaining = loginAttemptService.getRemainingBlockTime(email);
            throw new RuntimeException("Account locked! Try again after " + remaining + " minutes.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            long attempts = loginAttemptService.recordFailedAttempt(email);
            int remainingAttempts = (int) (7 - attempts);

            if (remainingAttempts <= 0) {
                throw new RuntimeException("Account locked! Try after 30 minutes.");
            }

            throw new RuntimeException("Invalid password. " + remainingAttempts + " attempts remaining.");
        }

        loginAttemptService.resetAttempts(email);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail());
    }
}