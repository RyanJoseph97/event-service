package com.eventmaster.controller;

import com.eventmaster.model.EventRsvp;
import com.eventmaster.model.RsvpRequest;
import com.eventmaster.model.RsvpSummaryResponse;
import com.eventmaster.service.RsvpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/events")
public class RsvpController {

    @Autowired
    private RsvpService rsvpService;

    @PostMapping("/{id}/rsvp")
    public ResponseEntity<EventRsvp> upsertRsvp(@PathVariable Long id,
                                                 @Valid @RequestBody RsvpRequest request,
                                                 Authentication authentication) {
        return ResponseEntity.ok(rsvpService.upsertRsvp(id, authentication.getName(), request.getStatus()));
    }

    @DeleteMapping("/{id}/rsvp")
    public ResponseEntity<Void> removeRsvp(@PathVariable Long id, Authentication authentication) {
        rsvpService.removeRsvp(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/rsvps")
    public ResponseEntity<RsvpSummaryResponse> getRsvpSummary(@PathVariable Long id) {
        return ResponseEntity.ok(rsvpService.getSummary(id));
    }

    @GetMapping("/{id}/rsvps/me")
    public ResponseEntity<EventRsvp> getMyRsvp(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return rsvpService.getMyRsvp(id, authentication.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
