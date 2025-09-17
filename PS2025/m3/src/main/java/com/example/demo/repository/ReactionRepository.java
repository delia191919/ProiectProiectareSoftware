package com.example.demo.repository;

import com.example.demo.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, String targetType);
    List<Reaction> findByTargetIdAndTargetType(Long targetId, String targetType);
}
