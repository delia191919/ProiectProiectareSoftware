package com.example.demo.dto.postdto;

import com.example.demo.dto.commentdto.CommentViewDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostViewDTO {
    private Long id;
    private Long userId;
    private String content;
    private Set<String> hashtags;
    private String image;
    private List<CommentViewDTO> comments;
    private Map<String, Long> reactions;
}