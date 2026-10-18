package com.example.securevault.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    private static final int OTP_TTL_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private final SecureRandom random = new SecureRandom();

    private String otpKey(String email) {
        return "otp:" + email;
    }

    private String attemptsKey(String email) {
        return "otp_attempts:" + email;
    }

    public void generateAndSend(String email) {
        String otp = String.format("%06d", random.nextInt(1_000_000));

        redisTemplate.opsForValue().set(otpKey(email), otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.delete(attemptsKey(email));
        
        emailService.sendOtp(email, otp);
    }

    public void verify(String email, String otp) {
        String stored = redisTemplate.opsForValue().get(otpKey(email));

        if (stored == null) {
            throw new RuntimeException("OTP expired. Please login again.");
        }

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey(email));
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(attemptsKey(email), OTP_TTL_MINUTES, TimeUnit.MINUTES);
        }

        if (attempts != null && attempts > MAX_ATTEMPTS) {
            redisTemplate.delete(otpKey(email));
            redisTemplate.delete(attemptsKey(email));
            throw new RuntimeException("Too many wrong attempts, OTP locked. Please login again.");
        }

        String given = otp == null ? "" : otp;
        boolean match = MessageDigest.isEqual(
                stored.getBytes(StandardCharsets.UTF_8),
                given.getBytes(StandardCharsets.UTF_8)
        );

        if (!match) {
            long remaining = MAX_ATTEMPTS - (attempts == null ? 0 : attempts);
            throw new RuntimeException("Invalid OTP. " + remaining + " attempts remaining.");
        }

        redisTemplate.delete(otpKey(email));
        redisTemplate.delete(attemptsKey(email));
    }
}