package com.example.securevault.service;

import com.example.securevault.model.Secret;
import com.example.securevault.repository.SecretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.example.securevault.util.EncryptionUtil;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final StringRedisTemplate redisTemplate;
    private final SecretRepository secretRepository;
    private final EncryptionUtil encryptionUtil;

    public String generateShareLink(Long secretId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                "share:" + token,
                secretId.toString(),
                1,
                TimeUnit.HOURS
        );
        return "http://localhost:8080/share/" + token;
    }

    public String accessSharedSecret(String token) throws Exception {
        String key = "share:" + token;
        String secretId = redisTemplate.opsForValue().get(key);

        if (secretId == null) {
            throw new RuntimeException("Link expired or invalid");
        }

        redisTemplate.delete(key);

        Secret secret = secretRepository.findById(Long.parseLong(secretId))
                .orElseThrow(() -> new RuntimeException("Secret not found"));

        return encryptionUtil.decrypt(secret.getEncryptedValue());
    }
}