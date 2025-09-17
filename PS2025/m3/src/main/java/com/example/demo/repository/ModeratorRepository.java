package com.example.demo.repository;

import com.example.demo.entity.Moderator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModeratorRepository extends JpaRepository<Moderator, String> {
    boolean existsByUserId(String userId);
}
