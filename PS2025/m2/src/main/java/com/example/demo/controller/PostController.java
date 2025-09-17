package com.example.demo.controller;

import com.example.demo.dto.commentdto.CommentViewDTO;
import com.example.demo.dto.postdto.PostViewDTO;
import com.example.demo.service.CommentService;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final RestTemplate restTemplate;


    @GetMapping("/getAllPosts")
    public ResponseEntity<List<PostViewDTO>> getAllPosts() {
        return new ResponseEntity<>(postService.getAllPosts(), HttpStatus.OK);
    }

    @PostMapping(value = "/createPost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostViewDTO> createPost(@RequestParam("userId") Long userId,
                                                  @RequestParam("content") String content,
                                                  @RequestParam("hashtags") String hashtagsParam,
                                                  @RequestPart("image") MultipartFile image) throws IOException {

        Set<String> hashtags = Arrays.stream(hashtagsParam.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        byte[] imageBytes = image.getBytes();
        return new ResponseEntity<>(postService.createPost(userId, content, hashtags, imageBytes), HttpStatus.CREATED);
    }

    @PutMapping(value = "/updatePost/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostViewDTO> editPost(@PathVariable Long postId,
                                                @RequestParam("content") String newContent,
                                                @RequestParam(value = "hashtags", required = false) String newHashtags,
                                                @RequestPart(value = "image", required = false) MultipartFile newImage) throws IOException {

        try {
            Set<String> updatedHashtags = null;
            if (newHashtags != null) {
                updatedHashtags = Arrays.stream(newHashtags.split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());
            }

            byte[] imageBytes = null;
            if (newImage != null) {
                imageBytes = newImage.getBytes();
            }

            PostViewDTO updatedPost = postService.updatePost(postId, newContent, updatedHashtags, imageBytes);
            return new ResponseEntity<>(updatedPost, HttpStatus.OK);
        }
        catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("deletePost/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    // Filtrează postările după un hashtag
    @GetMapping("/filter/hashtag/{hashtag}")
    public ResponseEntity<List<PostViewDTO>> filterPostsByHashtag(@PathVariable("hashtag") String hashtag) {
        return new ResponseEntity<>(postService.filterPostsByHashtag(hashtag), HttpStatus.OK);
    }


    @GetMapping("/filter/content/{content}")
    public ResponseEntity<List<PostViewDTO>> filterPostsByContent(@PathVariable("content")  String content) {
        return new ResponseEntity<>(postService.filterPostsByContent(content), HttpStatus.OK);
    }

    @GetMapping("/filter/user/{id}")
    public ResponseEntity<List<PostViewDTO>> filterPostsByUser(@PathVariable("id") Long id) {
        return new ResponseEntity<>(postService.filterPostsByUser(id), HttpStatus.OK);
    }

    @GetMapping("/getPost/{postId}")
    public ResponseEntity<PostViewDTO> getPost(@PathVariable("postId") Long postId) {
        return new ResponseEntity<>(postService.getPost(postId), HttpStatus.OK);
    }


    @PostMapping(value = "/addComment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentViewDTO> addComment(@RequestParam("postId") Long postId,
                                                     @RequestParam("userId") Long userId,
                                                     @RequestParam("content") String content,
                                                     @RequestPart("image") MultipartFile image) throws IOException {

        byte[] imageBytes = image.getBytes();
        return new ResponseEntity<>(commentService.addComment(postId, userId, content, imageBytes), HttpStatus.CREATED);
    }

    @PutMapping(value = "/updateComment/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentViewDTO> editComment(@PathVariable Long id,
                                                      @RequestParam(value = "userId") Long userId,
                                                      @RequestParam(value = "content", required = false) String newContent,
                                                      @RequestPart(value = "image", required = false) MultipartFile newImage) throws IOException {
        try {

            byte[] imageBytes = null;
            if (newImage != null) {
                imageBytes = newImage.getBytes();
            }

            CommentViewDTO updatedComment = commentService.editComment(id, userId, newContent, imageBytes);
            return new ResponseEntity<>(updatedComment, HttpStatus.OK);
        }
        catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("deleteComment/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/getComment/{commentId}")
    public ResponseEntity<CommentViewDTO> getComment(@PathVariable("commentId") Long commentId) {
        return new ResponseEntity<>(commentService.getComment(commentId), HttpStatus.OK);
    }

    @GetMapping("/getPostWithReactions/{postId}")
    public ResponseEntity<PostViewDTO> getPostWithReactions(@PathVariable Long postId) {
        PostViewDTO post = postService.getPost(postId);

        String url = "http://localhost:8083/api/reactions/count?targetId=" + postId + "&targetType=POST";

        ResponseEntity<Map<String, Integer>> response = restTemplate.getForEntity(
                url,
                (Class<Map<String, Integer>>) (Class<?>) Map.class
        );

        Map<String, Long> converted = new HashMap<>();
        if (response.getBody() != null) {
            response.getBody().forEach((key, value) -> converted.put(key, value.longValue()));
        }

        post.setReactions(converted);

        return new ResponseEntity<>(post, HttpStatus.OK);
    }


    @GetMapping("/getCommentWithReactions/{commentId}")
    public ResponseEntity<CommentViewDTO> getCommentWithReactions(@PathVariable Long commentId,
                                                                  @RequestHeader(value = "Authorization", required = false) String token) {
        CommentViewDTO comment = commentService.getComment(commentId);

        String url = "http://localhost:8083/api/reactions/count?targetId=" + commentId + "&targetType=COMMENT";

        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.set("Authorization", token);
        }

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Integer>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                (Class<Map<String, Integer>>) (Class<?>) Map.class
        );

        Map<String, Long> converted = new HashMap<>();
        if (response.getBody() != null) {
            response.getBody().forEach((key, value) -> converted.put(key, value.longValue()));
        }

        comment.setReactions(converted);

        return new ResponseEntity<>(comment, HttpStatus.OK);
    }




    @PostMapping("/addReaction")
    public ResponseEntity<Void> addReaction(@RequestParam("userId") Long userId,
                                            @RequestParam("postId") Long postId,
                                            @RequestParam("reactionType") String reactionType,
                                            @RequestHeader("Authorization") String bearerToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        restTemplate.exchange(
                "\"http://localhost:8083/api/reactions/addOrUpdate?...\"\n" + userId +
                        "&targetId=" + postId +
                        "&targetType=POST" +
                        "&reactionType=" + reactionType,
                HttpMethod.POST,
                requestEntity,
                Void.class
        );

        return ResponseEntity.status(201).build();
    }
}



