package com.example.demo.service;

import com.example.demo.entity.Reaction;
import com.example.demo.entity.ReactionType;
import com.example.demo.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;

    public void addOrUpdateReaction(Long userId, Long targetId, String targetType, String reactionTypeString) {
        ReactionType reactionType = ReactionType.valueOf(reactionTypeString.toUpperCase());

        Optional<Reaction> existingReactionOpt = reactionRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType);

        if (existingReactionOpt.isPresent()) {
            Reaction existing = existingReactionOpt.get();
            existing.setReactionType(reactionType);
            existing.setTimestamp(LocalDateTime.now());
            reactionRepository.save(existing);
        } else {
            Reaction newReaction = new Reaction();
            newReaction.setUserId(userId);
            newReaction.setTargetId(targetId);
            newReaction.setTargetType(targetType);
            newReaction.setReactionType(reactionType);
            newReaction.setTimestamp(LocalDateTime.now());
            reactionRepository.save(newReaction);
        }
    }

    public Map<String, Long> countReactionsByType(Long targetId, String targetType) {
        List<Reaction> reactions = reactionRepository.findByTargetIdAndTargetType(targetId, targetType);

        return reactions.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getReactionType().name(),
                        Collectors.counting()
                ));
    }
}
