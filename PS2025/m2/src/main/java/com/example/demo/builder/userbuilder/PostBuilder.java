package com.example.demo.builder.userbuilder;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;

import java.time.LocalDateTime;
import java.util.HashSet;

public class PostBuilder {
    public static Post generatePostFromDTO(String content, User user) {

        return Post.builder()
                .user(user)
                .content(content)
                .timeStamp(LocalDateTime.now())
                .hashtags(new HashSet<>())
                .build();
    }
}