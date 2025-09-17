package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long targetId;      // ID-ul postării sau comentariului
    private String targetType;  // "POST" sau "COMMENT"

    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;

    private LocalDateTime timestamp;

    // --- Getters ---
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public ReactionType getReactionType() {
        return reactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // --- Setters ---
    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
