package com.example.securevault.repository;

import com.example.securevault.model.Secret;
import com.example.securevault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SecretRepository extends JpaRepository<Secret, Long> {
    List<Secret> findByOwner(User owner);
}