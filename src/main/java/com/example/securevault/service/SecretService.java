package com.example.securevault.service;

import com.example.securevault.dto.SecretRequest;
import com.example.securevault.dto.SecretResponse;
import com.example.securevault.model.Secret;
import com.example.securevault.model.User;
import com.example.securevault.repository.SecretRepository;
import com.example.securevault.repository.UserRepository;
import com.example.securevault.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.securevault.model.AccessLog;
import com.example.securevault.repository.AccessLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import com.example.securevault.model.AccessLog;
import java.util.List;



@Service
@RequiredArgsConstructor
public class SecretService {

    private final SecretRepository secretRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final AccessLogRepository accessLogRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public SecretResponse addSecret(SecretRequest request) throws Exception {
        User user = getCurrentUser();
        Secret secret = new Secret();
        secret.setName(request.getName());
        secret.setEncryptedValue(encryptionUtil.encrypt(request.getValue()));
        secret.setOwner(user);
        Secret saved = secretRepository.save(secret);
        return mapToResponse(saved, request.getValue());
    }

    public List<SecretResponse> getAllSecrets(HttpServletRequest request) throws Exception {
        User user = getCurrentUser();
        List<Secret> secrets = secretRepository.findByOwner(user);
        List<SecretResponse> responses = new java.util.ArrayList<>();
        for (Secret secret : secrets) {

            AccessLog log = new AccessLog();
            log.setSecret(secret);
            log.setUser(user);
            log.setIpAddress(request.getRemoteAddr());
            accessLogRepository.save(log);

            responses.add(mapToResponse(secret,
                    encryptionUtil.decrypt(secret.getEncryptedValue())));
        }
        return responses;
    }



    private SecretResponse mapToResponse(Secret secret, String plainValue) {
        SecretResponse response = new SecretResponse();
        response.setId(secret.getId());
        response.setName(secret.getName());
        response.setValue(plainValue);
        return response;
    }

    public List<AccessLog> getAccessLogs(Long secretId) {
        User user = getCurrentUser();
        Secret secret = secretRepository.findById(secretId)
                .orElseThrow(() -> new RuntimeException("Secret not found"));
        if (!secret.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        return accessLogRepository.findBySecret(secret);
    }

    public void deleteSecret(Long id) {
        User user = getCurrentUser();
        Secret secret = secretRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Secret not found"));
        if (!secret.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        // pehle logs delete karo
        List<AccessLog> logs = accessLogRepository.findBySecret(secret);
        accessLogRepository.deleteAll(logs);
        // phir secret delete karo
        secretRepository.delete(secret);
    }
}