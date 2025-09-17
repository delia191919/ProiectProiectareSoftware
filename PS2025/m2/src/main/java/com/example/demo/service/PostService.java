package com.example.demo.service;

import com.example.demo.builder.userbuilder.PostBuilder;
import com.example.demo.dto.commentdto.CommentViewDTO;
import com.example.demo.dto.postdto.PostViewDTO;
import com.example.demo.entity.Hashtag;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.HashtagRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;

    private final PostRepository postRepository;

    private final HashtagRepository hashtagRepository;

    public List<PostViewDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::convertToViewDTO)
                .collect(Collectors.toList());
    }

    private PostViewDTO convertToViewDTO(Post post) {
        PostViewDTO postViewDTO = new PostViewDTO();
        postViewDTO.setId(post.getId());
        postViewDTO.setUserId(post.getUser().getId());
        postViewDTO.setContent(post.getContent());
        postViewDTO.setHashtags(post.getHashtags().stream()
                .map(Hashtag::getName)
                .collect(Collectors.toSet()));
        String base64Image = Base64.getEncoder().encodeToString(post.getImage());
        postViewDTO.setImage(base64Image.length() > 30 ? base64Image.substring(0, 30) + "..." : base64Image);
        return postViewDTO;
    }

    private PostViewDTO convertToViewCommentsDTO(Post post) {
        PostViewDTO postViewDTO = new PostViewDTO();
        postViewDTO.setId(post.getId());
        postViewDTO.setUserId(post.getUser().getId());
        postViewDTO.setContent(post.getContent());
        postViewDTO.setHashtags(post.getHashtags().stream()
                .map(Hashtag::getName)
                .collect(Collectors.toSet()));
        String base64Image = Base64.getEncoder().encodeToString(post.getImage());
        postViewDTO.setImage(base64Image.length() > 20 ? base64Image.substring(0, 20) + "..." : base64Image);
        postViewDTO.setComments(post.getComments().stream()
                .map(comment -> {
                    CommentViewDTO commentViewDTO = new CommentViewDTO();
                    commentViewDTO.setId(comment.getId());
                    commentViewDTO.setUserId(comment.getUsercomment().getId());
                    commentViewDTO.setContent(comment.getContent());
                    String base64CommentImage = Base64.getEncoder().encodeToString(comment.getImage());
                    commentViewDTO.setImage(base64CommentImage.length() > 20 ? base64CommentImage.substring(0, 20) + "..." : base64CommentImage);
                    return commentViewDTO;
                })
                .collect(Collectors.toList()));
        return postViewDTO;
    }

    public PostViewDTO createPost(Long userID, String content, Set<String> hashtags, byte[] image) {
        Optional<User> user = userRepository.findById(userID);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userID);
        }

        Post savePost = PostBuilder.generatePostFromDTO(content, user.get());
        for (String tagName : hashtags) {
            Hashtag hashtag = hashtagRepository.findByName(tagName);
            if (hashtag == null) {
                hashtag = new Hashtag();
                hashtag.setName(tagName);
                hashtagRepository.save(hashtag);
            }
            savePost.getHashtags().add(hashtag);
        }

        savePost.setImage(image);
        postRepository.save(savePost);
        return convertToViewDTO(savePost);
    }

    public PostViewDTO updatePost(Long postId, String newContent, Set<String> newHashtags, byte[] newImage) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        if (newContent != null) {
            post.setContent(newContent);
        }

        if (newImage != null) {
            post.setImage(newImage);
        }

        if (newHashtags != null) {
            Set<Hashtag> hashtagEntities = newHashtags.stream()
                    .map(tagName -> {
                        Hashtag hashtag = hashtagRepository.findByName(tagName);
                        if (hashtag == null) {
                            hashtag = new Hashtag();
                            hashtag.setName(tagName);
                            hashtagRepository.save(hashtag);
                        }
                        return hashtag;
                    })
                    .collect(Collectors.toSet());
            post.setHashtags(hashtagEntities);
        }

        postRepository.save(post);

        return convertToViewDTO(post);
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        post.getHashtags().clear();
        postRepository.save(post);

        postRepository.deleteById(postId);
    }

    public List<PostViewDTO> filterPostsByHashtag(String hashtag) {
        return postRepository.findByHashtagsName(hashtag).stream()
                .map(this::convertToViewDTO)
                .collect(Collectors.toList());
    }

    public List<PostViewDTO> filterPostsByContent(String content) {
        return postRepository.findByContentContaining(content).stream()
                .map(this::convertToViewDTO)
                .collect(Collectors.toList());
    }

    public List<PostViewDTO> filterPostsByUser(Long userId) {
        return postRepository.findByUserId(userId).stream()
                .map(this::convertToViewDTO)
                .collect(Collectors.toList());
    }

    public PostViewDTO getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        return convertToViewCommentsDTO(post);
    }
}