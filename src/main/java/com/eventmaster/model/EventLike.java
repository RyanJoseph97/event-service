package com.eventmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "username"})
})
public class EventLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String username;

    @Column(name = "liked_at", nullable = false)
    private LocalDateTime likedAt;

    public EventLike() {}

    public EventLike(Event event, String username) {
        this.event = event;
        this.username = username;
        this.likedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public String getUsername() { return username; }
    public LocalDateTime getLikedAt() { return likedAt; }
}
