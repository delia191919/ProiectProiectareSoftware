package com.example.demo.service;

import com.example.demo.entity.Moderator;
import com.example.demo.entity.User;
import com.example.demo.repository.ModeratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.example.demo.repository.UserRepository;

import java.util.Optional;

@Service
public class ModeratorService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    public boolean esteModerator(String moderatorId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:8081/api/user/utilizatori/" + moderatorId + "/rol",
                    HttpMethod.GET,
                    request,
                    String.class
            );

            return "MODERATOR".equalsIgnoreCase(response.getBody());
        } catch (HttpClientErrorException e) {
            System.err.println("Eroare la verificare rol: " + e.getStatusCode());
            return false;
        }
    }

    public boolean blocheazaUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) return false;
        user.get().setBlocat(true);
        userRepository.save(user.get());
        return true;
    }
    public boolean deblocheazaUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) return false;
        user.get().setBlocat(false);
        userRepository.save(user.get());
        return true;
    }



    @Autowired
    private ModeratorRepository moderatorRepository;

    public void adaugaModerator(String userId) {
        Moderator moderator = new Moderator(userId);
        moderatorRepository.save(moderator);
    }}
