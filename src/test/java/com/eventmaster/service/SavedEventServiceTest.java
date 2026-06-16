package com.eventmaster.service;

import com.eventmaster.exception.EventNotFoundException;
import com.eventmaster.exception.ForbiddenException;
import com.eventmaster.model.Event;
import com.eventmaster.model.SavedEvent;
import com.eventmaster.model.Visibility;
import com.eventmaster.repository.SavedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SavedEventServiceTest {

    @Mock
    private SavedEventRepository savedEventRepository;

    @Mock
    private EventService eventService;

    @InjectMocks
    private SavedEventService savedEventService;

    private Event event;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        event = new Event("Music Night", "Live bands", "6th Street",
                LocalDateTime.of(2025, 6, 1, 20, 0), null, 100, "alice", Visibility.PUBLIC);
        ReflectionTestUtils.setField(event, "id", 1L);
    }

    // --- save ---

    @Test
    public void save_success_savesSavedEvent() {
        when(eventService.findById(1L, "bob")).thenReturn(event);
        when(savedEventRepository.existsByUsernameAndEventId("bob", 1L)).thenReturn(false);

        savedEventService.save(1L, "bob");

        verify(savedEventRepository).save(any(SavedEvent.class));
    }

    @Test
    public void save_alreadySaved_throwsIllegalState() {
        when(eventService.findById(1L, "bob")).thenReturn(event);
        when(savedEventRepository.existsByUsernameAndEventId("bob", 1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> savedEventService.save(1L, "bob"));
        verify(savedEventRepository, never()).save(any());
    }

    @Test
    public void save_eventNotFound_throwsEventNotFound() {
        when(eventService.findById(99L, "bob")).thenThrow(new EventNotFoundException(99L));

        assertThrows(EventNotFoundException.class, () -> savedEventService.save(99L, "bob"));
        verify(savedEventRepository, never()).save(any());
    }

    // --- unsave ---

    @Test
    public void unsave_success_deletesRecord() {
        when(eventService.findById(1L, "bob")).thenReturn(event);
        when(savedEventRepository.existsByUsernameAndEventId("bob", 1L)).thenReturn(true);

        savedEventService.unsave(1L, "bob");

        verify(savedEventRepository).deleteByUsernameAndEventId("bob", 1L);
    }

    @Test
    public void unsave_notSaved_throwsIllegalState() {
        when(eventService.findById(1L, "bob")).thenReturn(event);
        when(savedEventRepository.existsByUsernameAndEventId("bob", 1L)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> savedEventService.unsave(1L, "bob"));
        verify(savedEventRepository, never()).deleteByUsernameAndEventId(any(), any());
    }

    @Test
    public void unsave_eventNotFound_throwsEventNotFound() {
        when(eventService.findById(99L, "bob")).thenThrow(new EventNotFoundException(99L));

        assertThrows(EventNotFoundException.class, () -> savedEventService.unsave(99L, "bob"));
    }

    // --- getSavedEvents ---

    @Test
    public void getSavedEvents_ownEvents_returnsEventList() {
        SavedEvent saved = new SavedEvent(event, "bob");
        when(savedEventRepository.findByUsernameWithEvent("bob")).thenReturn(List.of(saved));

        List<Event> result = savedEventService.getSavedEvents("bob", "bob");

        assertEquals(1, result.size());
        assertEquals("Music Night", result.get(0).getTitle());
    }

    @Test
    public void getSavedEvents_noSavedEvents_returnsEmptyList() {
        when(savedEventRepository.findByUsernameWithEvent("bob")).thenReturn(List.of());

        List<Event> result = savedEventService.getSavedEvents("bob", "bob");

        assertTrue(result.isEmpty());
    }

    @Test
    public void getSavedEvents_otherUserEvents_throwsForbidden() {
        assertThrows(ForbiddenException.class,
                () -> savedEventService.getSavedEvents("alice", "bob"));
        verify(savedEventRepository, never()).findByUsernameWithEvent(any());
    }
}
