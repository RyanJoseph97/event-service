package com.eventmaster.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_invites",
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "invitee_username"}))
public class EventInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "invitee_username", nullable = false)
    private String inviteeUsername;

    @Column(name = "invited_at", nullable = false)
    private LocalDateTime invitedAt;

    public EventInvite() {}

    public EventInvite(Event event, String inviteeUsername) {
        this.event = event;
        this.inviteeUsername = inviteeUsername;
        this.invitedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public String getInviteeUsername() { return inviteeUsername; }
    public LocalDateTime getInvitedAt() { return invitedAt; }
}
