package com.example.securevault.controller;

import com.example.securevault.dto.SecretRequest;
import com.example.securevault.dto.SecretResponse;
import com.example.securevault.service.SecretService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.example.securevault.model.AccessLog;

import java.util.List;

@RestController
@RequestMapping("/secrets")
@RequiredArgsConstructor
public class SecretController {

    private final SecretService secretService;

    @PostMapping
    public SecretResponse addSecret(@RequestBody SecretRequest request) throws Exception {
        return secretService.addSecret(request);
    }

    @DeleteMapping("/{id}")
    public String deleteSecret(@PathVariable Long id) {
        secretService.deleteSecret(id);
        return "Secret deleted";
    }
    @GetMapping
    public List<SecretResponse> getAllSecrets(HttpServletRequest request) throws Exception {
        return secretService.getAllSecrets(request);
    }

    @GetMapping("/{id}/logs")
    public List<AccessLog> getAccessLogs(@PathVariable Long id) {
        return secretService.getAccessLogs(id);
    }
}