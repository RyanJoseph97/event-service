package com.eventmaster.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UpdateEventRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Visibility visibility;

    @Size(max = 2048, message = "Image URL must not exceed 2048 characters")
    private String imageUrl;

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

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    private RecurrenceType recurrenceType;
    private LocalDate recurrenceEndDate;
    private EventCategory category;

    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(RecurrenceType recurrenceType) { this.recurrenceType = recurrenceType; }

    public LocalDate getRecurrenceEndDate() { return recurrenceEndDate; }
    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }

    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category; }

    private Double latitude;
    private Double longitude;

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
