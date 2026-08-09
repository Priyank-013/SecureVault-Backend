package com.example.securevault.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}