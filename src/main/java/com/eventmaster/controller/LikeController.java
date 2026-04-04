package com.eventmaster.controller;

import com.eventmaster.model.LikeCountResponse;
import com.eventmaster.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> like(@PathVariable Long id, Authentication authentication) {
        likeService.like(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlike(@PathVariable Long id, Authentication authentication) {
        likeService.unlike(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<LikeCountResponse> getLikeCount(@PathVariable Long id) {
        return ResponseEntity.ok(likeService.getLikeCount(id));
    }
}
