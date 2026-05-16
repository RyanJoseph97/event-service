package com.eventmaster.controller;

import com.eventmaster.exception.ForbiddenException;
import com.eventmaster.model.CreateEventRequest;
import com.eventmaster.model.Event;
import com.eventmaster.model.UpdateEventRequest;
import com.eventmaster.model.Visibility;
import com.eventmaster.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        logger.debug("GET /events/{}", id);
        return ResponseEntity.ok(eventService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Event>> getAllEvents(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String creatorUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startBefore,
            @RequestParam(required = false) Visibility visibility,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.debug("GET /events location={} creatorUsername={} startAfter={} startBefore={} visibility={}",
                location, creatorUsername, startAfter, startBefore, visibility);
        return ResponseEntity.ok(eventService.getAllEvents(location, creatorUsername, null, startAfter, startBefore, visibility, pageable));
    }

    @GetMapping("/by-creator/{username}")
    public ResponseEntity<List<Event>> getEventsByCreator(@PathVariable String username) {
        logger.debug("GET /events/by-creator/{}", username);
        return ResponseEntity.ok(eventService.findByCreatorUsername(username));
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody CreateEventRequest request,
                                             Authentication authentication) {
        String username = authentication.getName();
        Visibility visibility = request.getVisibility() != null ? request.getVisibility() : Visibility.PUBLIC;

        // Public events require VERIFIED or TRUSTED status.
        // Private (INVITE_ONLY) events can be created by any authenticated user.
        if (visibility == Visibility.PUBLIC) {
            assertVerifiedOrAbove(authentication.getAuthorities(), username);
        }

        logger.debug("POST /events from user: {} visibility: {}", username, visibility);
        return ResponseEntity.ok(eventService.createEvent(request, username));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id,
                                             @Valid @RequestBody UpdateEventRequest request,
                                             Authentication authentication) {
        logger.debug("PATCH /events/{} from user: {}", id, authentication.getName());
        return ResponseEntity.ok(eventService.updateEvent(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id,
                                            Authentication authentication) {
        logger.debug("DELETE /events/{} from user: {}", id, authentication.getName());
        eventService.deleteEvent(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    private void assertVerifiedOrAbove(Collection<? extends GrantedAuthority> authorities, String username) {
        boolean hasStatus = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("VERIFIED") || a.equals("TRUSTED"));
        if (!hasStatus) {
            logger.warn("User '{}' attempted to create a public event but is not verified", username);
            throw new ForbiddenException("Only verified users can create public events");
        }
    }
}
