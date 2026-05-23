package com.eventmaster.model;

import java.time.LocalDateTime;

public class EventSummaryResponse {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer capacity;
    private String creatorUsername;
    private LocalDateTime createdAt;
    private Visibility visibility;
    private String imageUrl;
    private long likeCount;
    private long goingCount;

    public EventSummaryResponse(Event event, long likeCount, long goingCount) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.location = event.getLocation();
        this.startTime = event.getStartTime();
        this.endTime = event.getEndTime();
        this.capacity = event.getCapacity();
        this.creatorUsername = event.getCreatorUsername();
        this.createdAt = event.getCreatedAt();
        this.visibility = event.getVisibility();
        this.imageUrl = event.getImageUrl();
        this.likeCount = likeCount;
        this.goingCount = goingCount;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Integer getCapacity() { return capacity; }
    public String getCreatorUsername() { return creatorUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Visibility getVisibility() { return visibility; }
    public String getImageUrl() { return imageUrl; }
    public long getLikeCount() { return likeCount; }
    public long getGoingCount() { return goingCount; }
}
