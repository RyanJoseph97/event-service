package com.eventmaster.controller;

import com.eventmaster.model.Comment;
import com.eventmaster.model.CommentResponse;
import com.eventmaster.model.CreateCommentRequest;
import com.eventmaster.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/events/{eventId}/comments")
    public ResponseEntity<Comment> createComment(@PathVariable Long eventId,
            @RequestBody @Valid CreateCommentRequest request, Authentication authentication) {
        Comment comment = commentService.createComment(eventId, request.getContent(), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/events/{eventId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long eventId,
            Authentication authentication) {
        return ResponseEntity.ok(commentService.getCommentsByEventId(eventId, viewerUsername(authentication)));
    }

    private String viewerUsername(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) return null;
        return authentication.getName();
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Long commentId, Authentication authentication) {
        commentService.likeComment(commentId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<Void> unlikeComment(@PathVariable Long commentId, Authentication authentication) {
        commentService.unlikeComment(commentId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
