package com.eventmaster.service;

import com.eventmaster.exception.EventNotFoundException;
import com.eventmaster.exception.ForbiddenException;
import com.eventmaster.model.CreateEventRequest;
import com.eventmaster.model.Event;
import com.eventmaster.model.UpdateEventRequest;
import com.eventmaster.model.Visibility;
import com.eventmaster.repository.CommentLikeRepository;
import com.eventmaster.repository.CommentRepository;
import com.eventmaster.repository.EventInviteRepository;
import com.eventmaster.repository.EventLikeRepository;
import com.eventmaster.repository.EventRepository;
import com.eventmaster.repository.EventRsvpRepository;
import com.eventmaster.repository.SavedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EventLikeRepository eventLikeRepository;

    @Mock
    private EventRsvpRepository eventRsvpRepository;

    @Mock
    private SavedEventRepository savedEventRepository;

    @Mock
    private EventInviteRepository eventInviteRepository;

    @InjectMocks
    private EventService eventService;

    private Event sampleEvent;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        sampleEvent = new Event("Music Night", "Live bands", "6th Street",
                LocalDateTime.of(2025, 6, 1, 20, 0), null, 100, "alice", Visibility.PUBLIC);
        ReflectionTestUtils.setField(sampleEvent, "id", 1L);
    }

    // --- createEvent ---

    @Test
    public void createEvent_savesAndReturnsEvent() {
        CreateEventRequest request = buildCreateRequest("Code & Coffee", Visibility.PUBLIC);
        Event saved = new Event("Code & Coffee", "Coding together", "Library",
                LocalDateTime.of(2025, 7, 1, 10, 0), null, 20, "alice", Visibility.PUBLIC);

        when(eventRepository.save(any(Event.class))).thenReturn(saved);

        Event result = eventService.createEvent(request, "alice");

        assertNotNull(result);
        assertEquals("Code & Coffee", result.getTitle());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    public void createEvent_setsCreatorUsernameFromParameter() {
        CreateEventRequest request = buildCreateRequest("My Event", Visibility.INVITE_ONLY);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.createEvent(request, "bob");

        assertEquals("bob", result.getCreatorUsername());
    }

    @Test
    public void createEvent_defaultsVisibilityToPublicWhenNull() {
        CreateEventRequest request = buildCreateRequest("Null Visibility Event", null);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.createEvent(request, "alice");

        assertEquals(Visibility.PUBLIC, result.getVisibility());
    }

    @Test
    public void createEvent_withImageUrl_setsImageUrl() {
        CreateEventRequest request = buildCreateRequest("Image Event", Visibility.PUBLIC);
        request.setImageUrl("https://example.com/img.png");
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.createEvent(request, "alice");

        assertEquals("https://example.com/img.png", result.getImageUrl());
    }

    @Test
    public void createEvent_setsCreatedAt() {
        CreateEventRequest request = buildCreateRequest("Timed Event", Visibility.PUBLIC);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.createEvent(request, "alice");

        assertNotNull(result.getCreatedAt());
    }

    // --- findById ---

    @Test
    public void findById_found_returnsEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        Event result = eventService.findById(1L);

        assertNotNull(result);
        assertEquals("Music Night", result.getTitle());
    }

    @Test
    public void findById_notFound_throwsEventNotFoundException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.findById(99L));
    }

    // --- getAllEvents ---

    @Test
    @SuppressWarnings("unchecked")
    public void getAllEvents_noFilters_returnsList() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(sampleEvent)));

        Page<Event> result = eventService.getAllEvents(null, null, null, null, null, null, null, null, Pageable.unpaged(), null);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAllEvents_withFilters_passesSpecToRepository() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(sampleEvent)));

        Page<Event> result = eventService.getAllEvents(null, "Austin", "alice", null,
                LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59),
                Visibility.PUBLIC, null, Pageable.unpaged(), null);

        assertEquals(1, result.getTotalElements());
        verify(eventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // --- findByCreatorUsername ---

    @Test
    public void findByCreatorUsername_returnsMatchingEvents() {
        when(eventRepository.findByCreatorUsername("alice")).thenReturn(List.of(sampleEvent));

        List<Event> result = eventService.findByCreatorUsername("alice");

        assertEquals(1, result.size());
        assertEquals("alice", result.get(0).getCreatorUsername());
    }

    @Test
    public void findByCreatorUsername_noEvents_returnsEmptyList() {
        when(eventRepository.findByCreatorUsername("nobody")).thenReturn(List.of());

        assertTrue(eventService.findByCreatorUsername("nobody").isEmpty());
    }

    // --- updateEvent ---

    @Test
    public void updateEvent_asOwner_appliesChanges() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateEventRequest request = new UpdateEventRequest();
        ReflectionTestUtils.setField(request, "title", "Updated Title");
        ReflectionTestUtils.setField(request, "capacity", 50);

        Event result = eventService.updateEvent(1L, request, "alice");

        assertEquals("Updated Title", result.getTitle());
        assertEquals(50, result.getCapacity());
        assertEquals("Live bands", result.getDescription()); // unchanged
    }

    @Test
    public void updateEvent_nullFieldsNotApplied() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateEventRequest request = new UpdateEventRequest(); // all null

        Event result = eventService.updateEvent(1L, request, "alice");

        assertEquals("Music Night", result.getTitle());    // unchanged
        assertEquals("Live bands", result.getDescription()); // unchanged
    }

    @Test
    public void updateEvent_asNonOwner_throwsForbidden() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        assertThrows(ForbiddenException.class,
                () -> eventService.updateEvent(1L, new UpdateEventRequest(), "bob"));

        verify(eventRepository, never()).save(any());
    }

    @Test
    public void updateEvent_eventNotFound_throwsEventNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class,
                () -> eventService.updateEvent(99L, new UpdateEventRequest(), "alice"));
    }

    // --- deleteEvent ---

    @Test
    public void deleteEvent_asOwner_deletesEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        eventService.deleteEvent(1L, "alice");

        verify(eventRepository).delete(sampleEvent);
    }

    @Test
    public void deleteEvent_asNonOwner_throwsForbidden() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        assertThrows(ForbiddenException.class,
                () -> eventService.deleteEvent(1L, "bob"));

        verify(eventRepository, never()).delete(any());
    }

    @Test
    public void deleteEvent_eventNotFound_throwsEventNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class,
                () -> eventService.deleteEvent(99L, "alice"));
    }

    // --- helpers ---

    private CreateEventRequest buildCreateRequest(String title, Visibility visibility) {
        CreateEventRequest req = new CreateEventRequest();
        ReflectionTestUtils.setField(req, "title", title);
        ReflectionTestUtils.setField(req, "description", "Test description");
        ReflectionTestUtils.setField(req, "location", "Test location");
        ReflectionTestUtils.setField(req, "startTime", LocalDateTime.of(2025, 8, 1, 12, 0));
        ReflectionTestUtils.setField(req, "visibility", visibility);
        ReflectionTestUtils.setField(req, "category", com.eventmaster.model.EventCategory.SOCIAL);
        return req;
    }
}
