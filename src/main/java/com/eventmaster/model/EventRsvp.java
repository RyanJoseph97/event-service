package com.eventmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_rsvps", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "username"})
}, indexes = {
        @Index(name = "idx_event_rsvp_username", columnList = "username")
})
public class EventRsvp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RsvpStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public EventRsvp() {}

    public EventRsvp(Event event, String username, RsvpStatus status) {
        this.event = event;
        this.username = username;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public String getUsername() { return username; }

    public RsvpStatus getStatus() { return status; }
    public void setStatus(RsvpStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
