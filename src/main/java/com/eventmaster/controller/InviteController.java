package com.eventmaster.controller;

import com.eventmaster.model.InviteRequest;
import com.eventmaster.service.EventInviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/events")
public class InviteController {

    @Autowired
    private EventInviteService inviteService;

    @GetMapping("/{id}/invites")
    public ResponseEntity<List<String>> getInvitees(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(inviteService.getInvitees(id, authentication.getName()));
    }

    @PostMapping("/{id}/invites")
    public ResponseEntity<Void> invite(@PathVariable Long id,
                                       @Valid @RequestBody InviteRequest request,
                                       Authentication authentication) {
        inviteService.invite(id, request.getUsername(), authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/invites/{username}")
    public ResponseEntity<Void> revoke(@PathVariable Long id,
                                       @PathVariable String username,
                                       Authentication authentication) {
        inviteService.revoke(id, username, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
