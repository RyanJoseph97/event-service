package com.eventmaster.service;

import com.eventmaster.exception.EventNotFoundException;
import com.eventmaster.exception.ForbiddenException;
import com.eventmaster.model.CreateEventRequest;
import com.eventmaster.model.Event;
import com.eventmaster.model.UpdateEventRequest;
import com.eventmaster.model.Visibility;
import com.eventmaster.repository.EventRepository;
import com.eventmaster.repository.EventSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    public Event createEvent(CreateEventRequest request, String creatorUsername) {
        Event event = new Event(
                request.getTitle(),
                request.getDescription(),
                request.getLocation(),
                request.getStartTime(),
                request.getEndTime(),
                request.getCapacity(),
                creatorUsername,
                request.getVisibility()
        );
        event.setImageUrl(request.getImageUrl());
        Event saved = eventRepository.save(event);
        logger.info("Event created with id: {} by user: {}", saved.getId(), creatorUsername);
        return saved;
    }

    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    public List<Event> getAllEvents(String location, String creatorUsername,
                                    LocalDateTime startAfter, LocalDateTime startBefore,
                                    Visibility visibility) {
        Specification<Event> spec = Specification.where(null);
        if (location != null)         spec = spec.and(EventSpecification.locationContains(location));
        if (creatorUsername != null)  spec = spec.and(EventSpecification.creatorUsernameEquals(creatorUsername));
        if (startAfter != null)       spec = spec.and(EventSpecification.startAfter(startAfter));
        if (startBefore != null)      spec = spec.and(EventSpecification.startBefore(startBefore));
        if (visibility != null)       spec = spec.and(EventSpecification.visibilityEquals(visibility));
        return eventRepository.findAll(spec);
    }

    public List<Event> findByCreatorUsername(String creatorUsername) {
        return eventRepository.findByCreatorUsername(creatorUsername);
    }

    public Event updateEvent(Long id, UpdateEventRequest request, String requesterUsername) {
        Event event = findById(id);
        if (!event.getCreatorUsername().equals(requesterUsername)) {
            throw new ForbiddenException("You do not have permission to modify this event");
        }
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        if (request.getCapacity() != null) event.setCapacity(request.getCapacity());
        if (request.getVisibility() != null) event.setVisibility(request.getVisibility());
        if (request.getImageUrl() != null) event.setImageUrl(request.getImageUrl());
        Event updated = eventRepository.save(event);
        logger.info("Event {} updated by user: {}", id, requesterUsername);
        return updated;
    }

    public void deleteEvent(Long id, String requesterUsername) {
        Event event = findById(id);
        if (!event.getCreatorUsername().equals(requesterUsername)) {
            throw new ForbiddenException("You do not have permission to delete this event");
        }
        eventRepository.delete(event);
        logger.info("Event {} deleted by user: {}", id, requesterUsername);
    }
}
