package com.example.demo.controller;

import com.example.demo.service.ModeratorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/moderatori")
public class ModeratorController {

    @Autowired
    private ModeratorService moderatorService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<String> adaugaModerator(@RequestParam String userId) {
        moderatorService.adaugaModerator(userId);
        return ResponseEntity.ok("Moderator adaugat");
    }

    @DeleteMapping("/postari/{postId}")
    public ResponseEntity<String> stergePostare(
            @PathVariable String postId,
            @RequestParam String moderatorId,
            @RequestHeader("Authorization") String token) {

        if (!moderatorService.esteModerator(moderatorId, token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nu esti moderator");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        restTemplate.exchange(
                "http://localhost:8082/api/posts/deletePost/" + postId,
                HttpMethod.DELETE,
                requestEntity,
                String.class
        );

        return ResponseEntity.ok("Postare stearsa");
    }

    @PostMapping("/blocheaza/{userId}")
    public ResponseEntity<String> blocheazaUserProxy(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token,
            @RequestParam String moderatorId) {

        if (!moderatorService.esteModerator(moderatorId, token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nu esti moderator");
        }

        boolean success = moderatorService.blocheazaUser(userId);
        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilizator inexistent");
        }

        return ResponseEntity.ok("Utilizator blocat cu succes");
    }

    @DeleteMapping("/comentarii/{comentariuId}")
    public ResponseEntity<String> stergeComentariu(
            @PathVariable String comentariuId,
            @RequestParam String moderatorId,
            @RequestHeader("Authorization") String token) {

        if (!moderatorService.esteModerator(moderatorId, token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nu esti moderator");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        restTemplate.exchange(
                "http://localhost:8082/api/posts/deleteComment/" + comentariuId,
                HttpMethod.DELETE,
                requestEntity,
                String.class
        );

        return ResponseEntity.ok("Comentariu sters");
    }


//    @PostMapping("/deblocheaza/{userId}")
//    public ResponseEntity<String> deblocheazaUser(
//            @PathVariable String userId,
//            @RequestParam String moderatorId,
//            @RequestHeader("Authorization") String token) {
//
//        if (!moderatorService.esteModerator(moderatorId, token)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nu esti moderator");
//        }
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + token.replace("Bearer ", ""));
//        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
//        //in loc sa trimirta catre rezolvare la m1, fa in service o noua metoda pentru debblocare si trimite inapoi doar raspunsul
//        try {
//            restTemplate.exchange(
//                    "http://localhost:8081/api/user/utilizatori/" + userId + "/deblocheaza",
//                    HttpMethod.POST,
//                    requestEntity,
//                    String.class
//            );
//
//            return ResponseEntity.ok("Utilizator deblocat");
//        } catch (HttpClientErrorException e) {
//            return ResponseEntity.status(e.getStatusCode()).body("Eroare: " + e.getStatusCode());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Eroare: " + e.getMessage());
//        }
//    }

    @PostMapping("/deblocheaza/{userId}")
    public ResponseEntity<String> deblocheazaUserLocal(
            @PathVariable Long userId,
            @RequestParam String moderatorId,
            @RequestHeader("Authorization") String token) {

        if (!moderatorService.esteModerator(moderatorId, token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nu esti moderator");
        }

        boolean success = moderatorService.deblocheazaUser(userId);
        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilizator inexistent");
        }

        return ResponseEntity.ok("Utilizator deblocat cu succes");
    }


}