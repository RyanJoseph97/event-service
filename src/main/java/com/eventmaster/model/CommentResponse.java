package com.eventmaster.model;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private Long eventId;
    private String username;
    private String profilePictureUrl;
    private String content;
    private LocalDateTime createdAt;

    public CommentResponse(Comment comment, String profilePictureUrl) {
        this.id = comment.getId();
        this.eventId = comment.getEventId();
        this.username = comment.getUsername();
        this.profilePictureUrl = profilePictureUrl;
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public String getUsername() { return username; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
