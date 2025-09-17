package com.example.demo.builder.userbuilder;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;

import java.time.LocalDateTime;


public class CommentBuilder{
    public static Comment generateCommentFromDTO(String content, User user, Post post) {
        return Comment.builder()
                .usercomment(user)
                .content(content)
                .timeStamp(LocalDateTime.now())
                .post(post)
                .build();
    }
}