package com.example.securevault.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_ATTEMPTS = 7;
    private static final int LOCK_MINUTES = 30;

    private String attemptsKey(String email) {
        return "login_attempts:" + email;
    }

    private String lockKey(String email) {
        return "login_locked:" + email;
    }

    public boolean isLocked(String email) {
        Boolean hasKey = redisTemplate.hasKey(lockKey(email));
        System.out.println("🔍 isLocked check for " + email + ": " + (hasKey != null && hasKey));
        return Boolean.TRUE.equals(hasKey);
    }

    public int getAttempts(String email) {
        String key = attemptsKey(email);
        String value = redisTemplate.opsForValue().get(key);
        int attempts = value == null ? 0 : Integer.parseInt(value);
        System.out.println("🔍 Attempts for " + email + ": " + attempts);
        return attempts;
    }

    public long getRemainingBlockTime(String email) {
        String key = lockKey(email);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
        return ttl != null ? ttl : 0;
    }

    public long recordFailedAttempt(String email) {
        String key = attemptsKey(email);
        Long attempts = redisTemplate.opsForValue().increment(key);
        System.out.println("📝 Recording failed attempt for " + email + ": " + attempts);

        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(key, LOCK_MINUTES, TimeUnit.MINUTES);
        }

        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            System.out.println("🔒 LOCKING ACCOUNT: " + email);
            redisTemplate.opsForValue().set(lockKey(email), "locked", LOCK_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(key);
        }

        return attempts != null ? attempts : 0;
    }

    public void resetAttempts(String email) {
        System.out.println("🔄 Resetting attempts for: " + email);
        redisTemplate.delete(attemptsKey(email));
        redisTemplate.delete(lockKey(email));
    }
}