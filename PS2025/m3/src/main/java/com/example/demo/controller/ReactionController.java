package com.example.demo.controller;

import com.example.demo.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/addOrUpdate")
    public ResponseEntity<Void> addOrUpdateReaction(@RequestParam Long userId,
                                                    @RequestParam Long targetId,
                                                    @RequestParam String targetType, // POST sau COMMENT
                                                    @RequestParam String reactionType) {
        reactionService.addOrUpdateReaction(userId, targetId, targetType, reactionType);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countReactions(@RequestParam Long targetId,
                                                            @RequestParam String targetType) {
        return ResponseEntity.ok(reactionService.countReactionsByType(targetId, targetType));
    }
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<Map<String, Long>> getReactionsForComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(reactionService.countReactionsByType(commentId, "COMMENT"));
    }

}
