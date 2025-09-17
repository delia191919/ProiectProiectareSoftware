package com.example.demo.dto.postdto;

import com.example.demo.dto.commentdto.CommentViewDTO;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class PostViewDTO {

    private Long id;

    private Long userId;

    private String content;

    private Set<String> hashtags;

    private String image;

    private List<CommentViewDTO> comments;

    private Map<String, Long> reactions;

    public Map<String, Long> getReactions() {
        return reactions;
    }

    public void setReactions(Map<String, Long> reactions) {
        this.reactions = reactions;
    }



}