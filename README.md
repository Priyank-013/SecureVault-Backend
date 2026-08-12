# SecureVault — Backend

Backend services for SecureVault, a developer secrets manager that lets users securely store, encrypt, and share API keys, passwords, and environment variables — with built-in rate limiting and brute-force protection.

## Features

- JWT-based authentication with Spring Security
- AES-256-GCM encryption for all stored secrets — plaintext never touches the database
- One-time, self-destructing share links with QR code support via Redis
- Access logging — track when and from where each secret was viewed
- Rate limiting — 100 requests/minute per IP (Redis-backed, sliding window)
- Brute-force protection — accounts lock for 30 minutes after 7 failed login attempts
- Global exception handling with meaningful HTTP status codes
- REST API endpoints for full secret lifecycle management

## Tech Stack

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA (MySQL)
- Redis (rate limiting, brute-force tracking, one-time share links)
- JWT (jjwt)
- Maven

 ## Database Models

- **User** — id, email, password_hash, name, timestamps
- **Secret** — id, user_id, name, encrypted_value, encryption_iv, timestamps
- **AccessLog** — id, secret_id, accessed_at, ip_address 

## Related Repositories

**Frontend:** [SecureVault-frontend]([https://github.com/YOUR-USERNAME/secure-vault-frontend](https://github.com/Priyank-013/SecureVault-Frontend))
