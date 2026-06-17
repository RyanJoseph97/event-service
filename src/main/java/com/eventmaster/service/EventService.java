package com.eventmaster.service;

import com.eventmaster.exception.EventNotFoundException;
import com.eventmaster.exception.ForbiddenException;
import com.eventmaster.model.CreateEventRequest;
import com.eventmaster.model.Event;
import com.eventmaster.model.EventCategory;
import com.eventmaster.model.EventSummaryResponse;
import com.eventmaster.model.RecurrenceType;
import com.eventmaster.model.RsvpStatus;
import com.eventmaster.model.UpdateEventRequest;
import com.eventmaster.model.Visibility;
import com.eventmaster.repository.CommentLikeRepository;
import com.eventmaster.repository.CommentRepository;
import com.eventmaster.repository.EventInviteRepository;
import com.eventmaster.repository.EventLikeRepository;
import com.eventmaster.repository.EventRepository;
import com.eventmaster.repository.EventRsvpRepository;
import com.eventmaster.repository.EventSpecification;
import com.eventmaster.repository.SavedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EventLikeRepository eventLikeRepository;

    @Autowired
    private EventRsvpRepository eventRsvpRepository;

    @Autowired
    private SavedEventRepository savedEventRepository;

    @Autowired
    private EventInviteRepository eventInviteRepository;

    @org.springframework.beans.factory.annotation.Value("${app.search.fts.enabled:false}")
    private boolean ftsEnabled;

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
        event.setRecurrenceType(request.getRecurrenceType() != null ? request.getRecurrenceType() : RecurrenceType.NONE);
        event.setRecurrenceEndDate(request.getRecurrenceEndDate());
        event.setCategory(request.getCategory() != null ? request.getCategory() : EventCategory.OTHER);
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        Event saved = eventRepository.save(event);
        logger.info("Event created with id: {} by user: {}", saved.getId(), creatorUsername);
        return saved;
    }

    private Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    public Event findById(Long id, String viewerUsername) {
        Event event = findById(id);
        if (event.getVisibility() == Visibility.INVITE_ONLY) {
            boolean canSee = viewerUsername != null && (
                    event.getCreatorUsername().equals(viewerUsername) ||
                    eventInviteRepository.existsByEventIdAndInviteeUsername(id, viewerUsername));
            if (!canSee) throw new ForbiddenException("This event is invite-only");
        }
        return event;
    }

    public Page<Event> getAllEvents(String keyword, String location, String creatorUsername,
                                    List<String> creatorUsernames,
                                    LocalDateTime startAfter, LocalDateTime startBefore,
                                    Visibility visibility, EventCategory category,
                                    Pageable pageable, String viewerUsername) {
        if (keyword != null && ftsEnabled) {
            List<Long> invitedIds = viewerUsername != null
                    ? eventInviteRepository.findEventIdsByInviteeUsername(viewerUsername)
                    : Collections.emptyList();
            List<Long> safeInvitedIds = invitedIds.isEmpty() ? Collections.singletonList(-1L) : invitedIds;
            return eventRepository.fullTextSearch(
                    keyword, location,
                    category != null ? category.name() : null,
                    startAfter, startBefore,
                    creatorUsername, viewerUsername,
                    safeInvitedIds, pageable);
        }
        Specification<Event> spec = Specification.where(null);
        if (keyword != null)                                         spec = spec.and(EventSpecification.keywordContains(keyword));
        if (location != null)                                        spec = spec.and(EventSpecification.locationContains(location));
        if (creatorUsernames != null && !creatorUsernames.isEmpty()) spec = spec.and(EventSpecification.creatorUsernameIn(creatorUsernames));
        else if (creatorUsername != null)                            spec = spec.and(EventSpecification.creatorUsernameEquals(creatorUsername));
        if (startAfter != null)                                      spec = spec.and(EventSpecification.startAfter(startAfter));
        if (startBefore != null)                                     spec = spec.and(EventSpecification.startBefore(startBefore));
        if (visibility != null)                                      spec = spec.and(EventSpecification.visibilityEquals(visibility));
        if (category != null)                                        spec = spec.and(EventSpecification.categoryEquals(category));
        if (viewerUsername == null) {
            spec = spec.and(EventSpecification.visibilityEquals(Visibility.PUBLIC));
        } else {
            List<Long> invitedIds = eventInviteRepository.findEventIdsByInviteeUsername(viewerUsername);
            spec = spec.and(EventSpecification.visibleTo(viewerUsername, invitedIds));
        }
        return eventRepository.findAll(spec, pageable);
    }

    public List<Event> findByCreatorUsername(String creatorUsername, String viewerUsername) {
        List<Event> events = eventRepository.findByCreatorUsername(creatorUsername);
        if (viewerUsername != null && viewerUsername.equals(creatorUsername)) {
            return events;
        }
        Set<Long> invitedIds = viewerUsername != null
                ? new HashSet<>(eventInviteRepository.findEventIdsByInviteeUsername(viewerUsername))
                : Collections.emptySet();
        return events.stream()
                .filter(e -> e.getVisibility() == Visibility.PUBLIC || invitedIds.contains(e.getId()))
                .collect(Collectors.toList());
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
        if (request.getRecurrenceType() != null) {
            event.setRecurrenceType(request.getRecurrenceType());
            if (request.getRecurrenceType() == RecurrenceType.NONE) event.setRecurrenceEndDate(null);
        }
        if (request.getRecurrenceEndDate() != null) event.setRecurrenceEndDate(request.getRecurrenceEndDate());
        if (request.getCategory() != null) event.setCategory(request.getCategory());
        if (request.getLatitude() != null) event.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) event.setLongitude(request.getLongitude());
        Event updated = eventRepository.save(event);
        logger.info("Event {} updated by user: {}", id, requesterUsername);
        return updated;
    }

    public EventSummaryResponse toSummary(Event event) {
        return toSummaries(List.of(event)).get(0);
    }

    public List<EventSummaryResponse> toSummaries(List<Event> events) {
        if (events.isEmpty()) return List.of();
        List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> likeCounts = eventLikeRepository.countGroupedByEventId(ids).stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        Map<Long, Long> goingCounts = eventRsvpRepository.countByStatusGroupedByEventId(ids, RsvpStatus.GOING).stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        return events.stream()
                .map(e -> new EventSummaryResponse(e,
                        likeCounts.getOrDefault(e.getId(), 0L),
                        goingCounts.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteEvent(Long id, String requesterUsername) {
        Event event = findById(id);
        if (!event.getCreatorUsername().equals(requesterUsername)) {
            throw new ForbiddenException("You do not have permission to delete this event");
        }
        // Delete child records in dependency order to avoid FK constraint violations.
        // CommentLikes reference Comments, so they must go first.
        commentLikeRepository.deleteByComment_EventId(id);
        commentRepository.deleteByEventId(id);
        eventLikeRepository.deleteByEventId(id);
        eventRsvpRepository.deleteByEventId(id);
        savedEventRepository.deleteByEventId(id);
        eventInviteRepository.deleteByEventId(id);
        eventRepository.delete(event);
        logger.info("Event {} deleted by user: {}", id, requesterUsername);
    }
}
