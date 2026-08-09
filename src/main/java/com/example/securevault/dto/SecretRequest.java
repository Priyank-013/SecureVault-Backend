package com.example.securevault.dto;

import lombok.Data;

@Data
public class SecretRequest {
    private String name;
    private String value;
}