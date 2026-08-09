package com.example.securevault.controller;

import com.example.securevault.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/secrets/{id}/share")
    public Map<String, String> generateLink(@PathVariable Long id) {
        String link = shareService.generateShareLink(id);
        return Map.of("link", link);
    }

    @GetMapping("/share/{token}")
    public String accessSecret(@PathVariable String token) throws Exception {
        return shareService.accessSharedSecret(token);
    }
}