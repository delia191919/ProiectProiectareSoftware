package com.example.demo.service;

import com.example.demo.builder.userbuilder.CommentBuilder;
import com.example.demo.dto.commentdto.CommentViewDTO;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    @Autowired
    private RestTemplate restTemplate;

    private final String m3BaseUrl = "http://localhost:8083/api/reactions/";

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private CommentViewDTO convertToViewDTO(Comment comment) {
        CommentViewDTO commentViewDTO = new CommentViewDTO();
        commentViewDTO.setId(comment.getId());
        commentViewDTO.setUserId(comment.getUsercomment().getId());
        commentViewDTO.setContent(comment.getContent());

        String base64Image = Base64.getEncoder().encodeToString(comment.getImage());
        commentViewDTO.setImage(base64Image.length() > 30 ? base64Image.substring(0, 30) + "..." : base64Image);

        try {
            ResponseEntity<Map<String, Long>> response = restTemplate.exchange(
                    m3BaseUrl + "comment/" + comment.getId(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            commentViewDTO.setReactions(response.getBody());
        } catch (Exception e) {
            System.out.println("Nu s-au putut incarca reactiile: " + e.getMessage());
            commentViewDTO.setReactions(null);
        }

        return commentViewDTO;
    }


    public CommentViewDTO addComment(Long postId, Long userId, String content, byte[] image) {
        Optional<User> user = userRepository.findById(userId);
        System.out.println("User found: " + user.isPresent());
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        Post post = postRepository.findById(postId).orElseThrow();
        Comment saveComment = CommentBuilder.generateCommentFromDTO(content, user.get(), post);
        saveComment.setImage(image);
        commentRepository.save(saveComment);
        return convertToViewDTO(saveComment);
    }

    public CommentViewDTO editComment(Long commentId, Long userId, String newContent, byte[] newImage) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));

        if (!comment.getUsercomment().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to edit this comment");
        }

        if (newContent != null && !newContent.isEmpty()) {
            comment.setContent(newContent);
        }

        if (newImage != null) {
            comment.setImage(newImage);
        }

        commentRepository.save(comment);
        return convertToViewDTO(comment);
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));

        Post post = comment.getPost();

        post.getComments().remove(comment);
        postRepository.save(post);

        commentRepository.deleteById(commentId);
    }

    public CommentViewDTO getComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));
        return convertToViewDTO(comment);
    }

}