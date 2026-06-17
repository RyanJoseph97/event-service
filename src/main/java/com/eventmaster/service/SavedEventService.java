package com.eventmaster.service;

import com.eventmaster.exception.ForbiddenException;
import com.eventmaster.model.Event;
import com.eventmaster.model.SavedEvent;
import com.eventmaster.repository.SavedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavedEventService {

    private static final Logger logger = LoggerFactory.getLogger(SavedEventService.class);

    @Autowired
    private SavedEventRepository savedEventRepository;

    @Autowired
    private EventService eventService;

    @Transactional
    public void save(Long eventId, String username) {
        Event event = eventService.findById(eventId, username);
        if (savedEventRepository.existsByUsernameAndEventId(username, eventId)) {
            throw new IllegalStateException("You have already saved this event");
        }
        try {
            savedEventRepository.save(new SavedEvent(event, username));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("You have already saved this event");
        }
        logger.info("User '{}' saved event {}", username, eventId);
    }

    @Transactional
    public void unsave(Long eventId, String username) {
        eventService.findById(eventId, username);
        if (!savedEventRepository.existsByUsernameAndEventId(username, eventId)) {
            throw new IllegalStateException("You have not saved this event");
        }
        savedEventRepository.deleteByUsernameAndEventId(username, eventId);
        logger.info("User '{}' unsaved event {}", username, eventId);
    }

    public List<Event> getSavedEvents(String requestedUsername, String authenticatedUsername) {
        if (!requestedUsername.equals(authenticatedUsername)) {
            throw new ForbiddenException("You can only view your own saved events");
        }
        return savedEventRepository.findByUsernameWithEvent(requestedUsername).stream()
                .map(SavedEvent::getEvent)
                .collect(Collectors.toList());
    }
}
