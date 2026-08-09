package com.example.securevault.repository;

import com.example.securevault.model.AccessLog;
import com.example.securevault.model.Secret;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    List<AccessLog> findBySecret(Secret secret);
}