package com.eventmaster.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_event_creator_username", columnList = "creator_username"),
        @Index(name = "idx_event_start_time", columnList = "start_time"),
        @Index(name = "idx_event_visibility", columnList = "visibility"),
        @Index(name = "idx_event_category", columnList = "category")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column
    private Integer capacity;

    @Column(name = "creator_username", nullable = false)
    private String creatorUsername;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false)
    private RecurrenceType recurrenceType = RecurrenceType.NONE;

    @Column(name = "recurrence_end_date")
    private LocalDate recurrenceEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory category = EventCategory.OTHER;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    public Event() {}

    public Event(String title, String description, String location,
                 LocalDateTime startTime, LocalDateTime endTime,
                 Integer capacity, String creatorUsername, Visibility visibility) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.creatorUsername = creatorUsername;
        this.createdAt = LocalDateTime.now();
        this.visibility = visibility != null ? visibility : Visibility.PUBLIC;
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getCreatorUsername() { return creatorUsername; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(RecurrenceType recurrenceType) { this.recurrenceType = recurrenceType; }

    public LocalDate getRecurrenceEndDate() { return recurrenceEndDate; }
    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }

    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
