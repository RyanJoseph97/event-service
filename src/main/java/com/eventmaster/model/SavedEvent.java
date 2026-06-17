package com.eventmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_events", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "username"})
}, indexes = {
        @Index(name = "idx_saved_event_username", columnList = "username")
})
public class SavedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String username;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    public SavedEvent() {}

    public SavedEvent(Event event, String username) {
        this.event = event;
        this.username = username;
        this.savedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public String getUsername() { return username; }
    public LocalDateTime getSavedAt() { return savedAt; }
}
