package com.example.securevault.dto;

import lombok.Data;

@Data
public class SecretResponse {
    private Long id;
    private String name;
    private String value;
}