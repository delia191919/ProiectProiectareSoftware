package com.example.demo.dto.commentdto;

import lombok.Data;

import java.util.Map;

@Data
public class CommentViewDTO {

    private Long id;

    private Long userId;

    private String content;

    private String image;
    private Map<String, Long> reactions;
}
