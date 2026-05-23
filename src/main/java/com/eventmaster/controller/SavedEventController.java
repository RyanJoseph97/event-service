package com.eventmaster.controller;

import com.eventmaster.model.Event;
import com.eventmaster.service.RsvpService;
import com.eventmaster.service.SavedEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SavedEventController {

    @Autowired
    private SavedEventService savedEventService;

    @Autowired
    private RsvpService rsvpService;

    @PostMapping("/events/{eventId}/save")
    public ResponseEntity<Void> save(@PathVariable Long eventId, Authentication authentication) {
        savedEventService.save(eventId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/events/{eventId}/save")
    public ResponseEntity<Void> unsave(@PathVariable Long eventId, Authentication authentication) {
        savedEventService.unsave(eventId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{username}/saved-events")
    public ResponseEntity<List<Event>> getSavedEvents(@PathVariable String username, Authentication authentication) {
        List<Event> events = savedEventService.getSavedEvents(username, authentication.getName());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/users/{username}/rsvped-events")
    public ResponseEntity<List<Event>> getRsvpedEvents(@PathVariable String username, Authentication authentication) {
        List<Event> events = rsvpService.getRsvpedEvents(username, authentication.getName());
        return ResponseEntity.ok(events);
    }
}
