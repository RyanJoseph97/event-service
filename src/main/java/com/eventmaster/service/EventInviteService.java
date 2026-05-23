package com.eventmaster.service;

import com.eventmaster.exception.ForbiddenException;
import com.eventmaster.model.Event;
import com.eventmaster.model.EventInvite;
import com.eventmaster.model.Visibility;
import com.eventmaster.repository.EventInviteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventInviteService {

    @Autowired
    private EventInviteRepository inviteRepository;

    @Autowired
    private EventService eventService;

    public List<String> getInvitees(Long eventId, String requesterUsername) {
        Event event = eventService.findById(eventId);
        if (!event.getCreatorUsername().equals(requesterUsername)) {
            throw new ForbiddenException("Only the event creator can view invites");
        }
        return inviteRepository.findByEventId(eventId).stream()
                .map(EventInvite::getInviteeUsername)
                .collect(Collectors.toList());
    }

    @Transactional
    public void invite(Long eventId, String inviteeUsername, String requesterUsername) {
        Event event = eventService.findById(eventId);
        if (!event.getCreatorUsername().equals(requesterUsername)) {
            throw new ForbiddenException("Only the event creator can send invites");
        }
        if (event.getVisibility() != Visibility.INVITE_ONLY) {
            throw new IllegalArgumentException("Invites only apply to INVITE ONLY events");
        }
        if (inviteeUsername.equals(requesterUsername)) {
            throw new IllegalArgumentException("You cannot invite yourself");
        }
        if (inviteRepository.existsByEventIdAndInviteeUsername(eventId, inviteeUsername)) {
            throw new IllegalStateException("User is already invited");
        }
        inviteRepository.save(new EventInvite(event, inviteeUsername));
    }

    @Transactional
    public void revoke(Long eventId, String inviteeUsername, String requesterUsername) {
        Event event = eventService.findById(eventId);
        if (!event.getCreatorUsername().equals(requesterUsername)) {
            throw new ForbiddenException("Only the event creator can revoke invites");
        }
        if (!inviteRepository.existsByEventIdAndInviteeUsername(eventId, inviteeUsername)) {
            throw new IllegalStateException("User is not invited");
        }
        inviteRepository.deleteByEventIdAndInviteeUsername(eventId, inviteeUsername);
    }
}
