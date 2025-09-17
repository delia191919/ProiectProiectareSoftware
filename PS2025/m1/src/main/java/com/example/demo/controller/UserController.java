package com.example.demo.controller;

import com.example.demo.dto.commentdto.CommentViewDTO;
import com.example.demo.dto.friendrequestdto.FriendRequestDTO;
import com.example.demo.dto.postdto.PostViewDTO;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FriendRequestService;
import com.example.demo.service.JWTService;
import com.example.demo.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;


import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTService jwtService;

    private final FriendRequestService friendRequestService;

    private final RestTemplate restTemplate;

    private final String postServiceUrl = "http://localhost:8082/api/posts/";
    private final String moderatorServiceUrl = "http://localhost:8083/moderatori/";


    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public ResponseEntity<?> displayAllUserView() {
        return new ResponseEntity<>(userService.findAllUserView(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getUserById/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> displayUserViewById(@PathVariable("id") @NonNull Long id) throws UserException {
        return new ResponseEntity<>(userService.findUserViewById(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/getUserByEmail/{email}", method = RequestMethod.GET)
    public ResponseEntity<?> displayUserViewByEmail(@PathVariable("email") String email) throws UserException {
        return new ResponseEntity<>(userService.findUserViewByEmail(email), HttpStatus.OK);
    }

    @RequestMapping(value = "/getUserByRoleName/{roleName}", method = RequestMethod.GET)
    public ResponseEntity<?> displayUserViewByRoleName(@PathVariable("roleName") String roleName) throws UserException {
        return new ResponseEntity<>(userService.findUserViewByRoleName(roleName), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/create")
    public ResponseEntity<?> processAddUserForm(@RequestBody(required = false) UserDTO userDTO) throws UserException {
        return new ResponseEntity<>(userService.createUser(userDTO), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/update")
    public ResponseEntity<?> processUpdateUserForm(@RequestBody UserDTO userDTO) throws UserException {
        return new ResponseEntity<>(userService.updateUser(userDTO), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUserByIdForm(@PathVariable("id") Long id) throws UserException {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/")
    public String hello(Authentication authentication) {
        String email = authentication.getName();
        return "Hello" + email;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        try {
            Authentication authManager = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );

            if (authManager.isAuthenticated()) {
                String token = jwtService.generateToken(user.getEmail());
                return ResponseEntity.ok(Collections.singletonMap("token", token));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Autentificare eșuată"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/friendRequest")
    public ResponseEntity<String> sendFriendRequest(@RequestBody FriendRequestDTO friendRequestDTO, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User sender = userService.findUserByEmail(userEmail);
            Long senderId = sender.getId();
            if (!senderId.equals(friendRequestDTO.getSenderId())) {
                return new ResponseEntity<>("Sender ID does not match authenticated user", HttpStatus.FORBIDDEN);
            }

            if (!userService.existsById(friendRequestDTO.getReceiverId())) {
                return new ResponseEntity<>("Receiver ID does not exist", HttpStatus.BAD_REQUEST);
            }

            String response = friendRequestService.sendFriendRequest(friendRequestDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/acceptFriendRequest")
    public ResponseEntity<String> acceptFriendRequest(@RequestBody FriendRequestDTO friendRequestDTO, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User receiver = userService.findUserByEmail(userEmail);
            Long receiverId = receiver.getId();
            if (!receiverId.equals(friendRequestDTO.getReceiverId())) {
                return new ResponseEntity<>("Receiver ID does not match authenticated user", HttpStatus.FORBIDDEN);
            }

            String response = friendRequestService.acceptFriendRequest(friendRequestDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/friends")
    public ResponseEntity<List<String>> getFriends(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userService.findUserByEmail(userEmail);
        Long userId = user.getId();
        List<String> friends = friendRequestService.getFriends(userId);
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getAllPosts")
    public ResponseEntity<List<?>> getAllPosts() {
        ResponseEntity<List<?>> response = restTemplate.getForEntity(postServiceUrl + "getAllPosts", (Class<List<?>>) (Class<?>) List.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getFriendsPosts")
    public ResponseEntity<List<PostViewDTO>> getFriendsPosts(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userService.findUserByEmail(userEmail);
        Long userId = user.getId();
        List<Long> friendsIds = friendRequestService.getFriendsId(userId);

        List<PostViewDTO> allPosts = friendsIds.stream()
                .flatMap(friendId -> {
                    ResponseEntity<List<PostViewDTO>> response = restTemplate.exchange(
                            postServiceUrl + "filter/user/" + friendId,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {
                            }
                    );
                    return response.getBody().stream();
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(allPosts, HttpStatus.OK);
    }

    //  Creează o postare nouă
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/createPost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(Authentication authentication,
                                        @RequestParam("content") String content,
                                        @RequestParam("hashtags") Set<String> hashtags,
                                        @RequestPart("image") MultipartFile image) throws IOException {
        String userEmail = authentication.getName();
        User user = userService.findUserByEmail(userEmail);
        Long userId = user.getId();
        byte[] imageBytes = image.getBytes();
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("userId", userId);
        body.add("content", content);
        body.add("hashtags", String.join(",", hashtags));
        body.add("image", imageResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<PostViewDTO> response = restTemplate.postForEntity(postServiceUrl + "createPost", requestEntity, PostViewDTO.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.CREATED);
    }

    @PutMapping("/editPost/{postId}")
    public ResponseEntity<?> editPost(@PathVariable Long postId,
                                      @RequestParam(value = "content", required = false) String newContent,
                                      @RequestParam(value = "hashtags", required = false) Set<String> newHashtags,
                                      @RequestPart(value = "image", required = false) MultipartFile newImage,
                                      Authentication authentication) throws IOException {

        String userEmail = authentication.getName();
        User authenticatedUser = userService.findUserByEmail(userEmail);
        Long authenticatedUserId = authenticatedUser.getId();

        PostViewDTO post = restTemplate.getForObject(postServiceUrl + "getPost/" + postId, PostViewDTO.class);
        if (post == null || !post.getUserId().equals(authenticatedUserId)) {
            return new ResponseEntity<>("You are not authorized to edit this post", HttpStatus.FORBIDDEN);
        }

        String updatedContent = (newContent != null) ? newContent : post.getContent();
        Set<String> updatedHashtags = (newHashtags != null) ? newHashtags : post.getHashtags();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("content", updatedContent);

        if (updatedHashtags != null && !updatedHashtags.isEmpty()) {
            body.add("hashtags", String.join(",", updatedHashtags));
        }

        if (newImage != null) {
            byte[] imageBytes = newImage.getBytes();
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return newImage.getOriginalFilename();
                }
            };
            body.add("image", imageResource);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String endpointUrl = postServiceUrl + "updatePost/" + postId;
        try {
            ResponseEntity<PostViewDTO> response = restTemplate.exchange(endpointUrl, HttpMethod.PUT, requestEntity, PostViewDTO.class);
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } catch (HttpClientErrorException.BadRequest e) {
            System.err.println("Bad Request: " + e.getResponseBodyAsString());
            return new ResponseEntity<>("Invalid request data", HttpStatus.BAD_REQUEST);
        } catch (HttpClientErrorException.Forbidden e) {
            return new ResponseEntity<>("You are not authorized to edit this post", HttpStatus.FORBIDDEN);
        } catch (HttpClientErrorException.NotFound e) {
            return new ResponseEntity<>("Post not found", HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/filter/hashtag/{hashtag}")
    public ResponseEntity<List<PostViewDTO>> filterPostsByHashtag(@PathVariable("hashtag") String hashtag) {
        ResponseEntity<List<PostViewDTO>> response = restTemplate.getForEntity(postServiceUrl + "filter/hashtag/" + hashtag, (Class<List<PostViewDTO>>) (Class<?>) List.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    @GetMapping("/filter/content/{content}")
    public ResponseEntity<List<PostViewDTO>> filterPostsByContent(@PathVariable("content") String content) {
        ResponseEntity<List<PostViewDTO>> response = restTemplate.getForEntity(postServiceUrl + "filter/content/" + content, (Class<List<PostViewDTO>>) (Class<?>) List.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    @GetMapping("/filter/user/{id}")
    public ResponseEntity<List<PostViewDTO>> filterPostsByUser(@PathVariable("id") Long id) {
        ResponseEntity<List<PostViewDTO>> response = restTemplate.getForEntity(postServiceUrl + "filter/user/" + id, (Class<List<PostViewDTO>>) (Class<?>) List.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    @GetMapping("/getPost/{postId}")
    public ResponseEntity<PostViewDTO> getPost(@PathVariable("postId") Long postId) {
        ResponseEntity<PostViewDTO> response = restTemplate.getForEntity(postServiceUrl + "getPost/" + postId, PostViewDTO.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/addComment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addComment(Authentication authentication,
                                        @RequestParam("postId") Long postId,
                                        @RequestParam("content") String content,
                                        @RequestPart("image") MultipartFile image) throws IOException {

        String userEmail = authentication.getName();
        User user = userService.findUserByEmail(userEmail);
        Long userId = user.getId();
        byte[] imageBytes = image.getBytes();
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("postId", postId);
        body.add("userId", userId);
        body.add("content", content);
        body.add("image", imageResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<CommentViewDTO> response = restTemplate.postForEntity(postServiceUrl + "addComment", requestEntity, CommentViewDTO.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.CREATED);
    }

    @PutMapping("/updateComment/{id}")
    public ResponseEntity<?> editComment(@PathVariable Long id,
                                         @RequestParam(value = "content", required = false) String newContent,
                                         @RequestPart(value = "image", required = false) MultipartFile newImage,
                                         Authentication authentication) throws IOException {

        String userEmail = authentication.getName();
        User authenticatedUser = userService.findUserByEmail(userEmail);
        Long authenticatedUserId = authenticatedUser.getId();

        CommentViewDTO existingComment = restTemplate.getForObject(postServiceUrl + "getComment/" + id, CommentViewDTO.class);
        if (existingComment == null || !existingComment.getUserId().equals(authenticatedUserId)) {
            return new ResponseEntity<>("You are not authorized to edit this comment", HttpStatus.FORBIDDEN);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        if (newContent != null) {
            body.add("content", newContent);
        }
        if (newImage != null) {
            byte[] imageBytes = newImage.getBytes();
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return newImage.getOriginalFilename();
                }
            };
            body.add("image", imageResource);
        }
        body.add("userId", authenticatedUserId);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(postServiceUrl + "updateComment/" + id, HttpMethod.PUT, requestEntity, CommentViewDTO.class);
            return new ResponseEntity<>("Comment updated successfully", HttpStatus.OK);
        } catch (HttpClientErrorException.Forbidden e) {
            return new ResponseEntity<>("You are not authorized to edit this comment", HttpStatus.FORBIDDEN);
        } catch (HttpClientErrorException.NotFound e) {
            return new ResponseEntity<>("Comment not found", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/deleteComment/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        restTemplate.delete(postServiceUrl + "deleteComment/" + id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "/reactToPost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> reactToPost(@RequestParam("userId") Long userId,
                                            @RequestParam("postId") Long postId,
                                            @RequestParam("reactionType") String reactionType,
                                            @RequestHeader(value = "Authorization", required = false) String bearerToken) {

        HttpHeaders headers = new HttpHeaders();
        if (bearerToken != null) {
            headers.set("Authorization", bearerToken);
        }


        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);


        String url = "http://localhost:8083/api/reactions/addOrUpdate?userId=" + userId +
                "&targetId=" + postId +
                "&targetType=POST" +
                "&reactionType=" + reactionType;

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Void.class
        );

        return ResponseEntity.status(201).build();
    }

    @PostMapping(value = "/reactToComment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> reactToComment(@RequestParam("userId") Long userId,
                                               @RequestParam("commentId") Long commentId,
                                               @RequestParam("reactionType") String reactionType,
                                               @RequestHeader(value = "Authorization", required = false) String bearerToken) {

        HttpHeaders headers = new HttpHeaders();
        if (bearerToken != null) {
            headers.set("Authorization", bearerToken);
        }

        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

        String url = "http://localhost:8083/api/reactions/addOrUpdate?userId=" + userId +
                "&targetId=" + commentId +
                "&targetType=COMMENT" +
                "&reactionType=" + reactionType;

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Void.class
        );

        return ResponseEntity.status(201).build();
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getPostWithReactions/{postId}")
    public ResponseEntity<PostViewDTO> getPostWithReactions(@PathVariable Long postId,
                                                            @RequestHeader("Authorization") String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PostViewDTO> response = restTemplate.exchange(
                postServiceUrl + "getPostWithReactions/" + postId,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<PostViewDTO>() {}
        );

        PostViewDTO post = response.getBody();

        System.out.println("REACTII PRIMITE LA POSTARE IN 8081: " + (post != null ? post.getReactions() : "null"));

        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getCommentWithReactions/{commentId}")
    public ResponseEntity<CommentViewDTO> getCommentWithReactions(@PathVariable Long commentId,
                                                                  @RequestHeader("Authorization") String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<CommentViewDTO> response = restTemplate.exchange(
                "http://localhost:8082/api/posts/getCommentWithReactions/" + commentId,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<CommentViewDTO>() {}
        );

        CommentViewDTO comment = response.getBody();

        return new ResponseEntity<>(comment, HttpStatus.OK);
    }
    @GetMapping("/utilizatori/{id}/rol")
    public ResponseEntity<String> getRol(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(user.get().getRole().getName());
    }

    @PostMapping("/mod/blocheaza/{userId}")
    public ResponseEntity<String> blocheazaDinM1(
            @PathVariable String userId,
            @RequestParam String moderatorId,
            @RequestHeader("Authorization") String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = moderatorServiceUrl + "blocheaza/" + userId + "?moderatorId=" + moderatorId;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body("Eroare de la M3: " + ex.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/mod/deblocheaza/{userId}")
    public ResponseEntity<String> deblocheazaDinM1(
            @PathVariable String userId,
            @RequestParam String moderatorId,
            @RequestHeader("Authorization") String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = moderatorServiceUrl + "deblocheaza/" + userId + "?moderatorId=" + moderatorId;

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            return ResponseEntity.ok("Utilizator deblocat (prin M3)");
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body("Eroare: " + ex.getResponseBodyAsString());
        }
    }

    @DeleteMapping("/mod/postari/{postId}")
    public ResponseEntity<String> stergePostareDinM1(
            @PathVariable String postId,
            @RequestParam String moderatorId,
            @RequestHeader("Authorization") String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = moderatorServiceUrl + "postari/" + postId + "?moderatorId=" + moderatorId;

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            return ResponseEntity.ok("Postare stearsa (prin M3)");
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body("Eroare: " + ex.getResponseBodyAsString());
        }
    }
    @DeleteMapping("/mod/comentarii/{comentariuId}")
    public ResponseEntity<String> stergeComentariuDinM1(
            @PathVariable String comentariuId,
            @RequestParam String moderatorId,
            @RequestHeader("Authorization") String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = moderatorServiceUrl + "comentarii/" + comentariuId + "?moderatorId=" + moderatorId;

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            return ResponseEntity.ok("Comentariu sters (prin M3)");
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body("Eroare: " + ex.getResponseBodyAsString());
        }
    }




}