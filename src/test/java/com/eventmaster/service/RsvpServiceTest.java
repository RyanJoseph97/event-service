package com.eventmaster.service;

import com.eventmaster.client.UserServiceClient;
import com.eventmaster.exception.EventNotFoundException;
import com.eventmaster.model.*;
import com.eventmaster.repository.EventRsvpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RsvpServiceTest {

    @Mock
    private EventRsvpRepository rsvpRepository;

    @Mock
    private EventService eventService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private RsvpService rsvpService;

    private Event event;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        event = new Event("Music Night", "Live bands", "6th Street",
                LocalDateTime.of(2025, 6, 1, 20, 0), null, 100, "alice", Visibility.PUBLIC);
        ReflectionTestUtils.setField(event, "id", 1L);
    }

    // --- upsertRsvp (create) ---

    @Test
    public void upsertRsvp_noExisting_createsNew() {
        when(eventService.findById(eq(1L), any())).thenReturn(event);
        when(rsvpRepository.findByEventIdAndUsername(1L, "bob")).thenReturn(Optional.empty());
        when(rsvpRepository.save(any(EventRsvp.class))).thenAnswer(inv -> inv.getArgument(0));

        EventRsvp result = rsvpService.upsertRsvp(1L, "bob", RsvpStatus.GOING);

        assertEquals(RsvpStatus.GOING, result.getStatus());
        assertEquals("bob", result.getUsername());
        verify(rsvpRepository).save(any(EventRsvp.class));
    }

    @Test
    public void upsertRsvp_existingRsvp_updatesStatus() {
        EventRsvp existing = new EventRsvp(event, "bob", RsvpStatus.INTERESTED);
        when(eventService.findById(eq(1L), any())).thenReturn(event);
        when(rsvpRepository.findByEventIdAndUsername(1L, "bob")).thenReturn(Optional.of(existing));
        when(rsvpRepository.save(any(EventRsvp.class))).thenAnswer(inv -> inv.getArgument(0));

        EventRsvp result = rsvpService.upsertRsvp(1L, "bob", RsvpStatus.GOING);

        assertEquals(RsvpStatus.GOING, result.getStatus());
        assertNotNull(result.getUpdatedAt()); // setStatus() sets updatedAt
    }

    @Test
    public void upsertRsvp_eventNotFound_throwsEventNotFound() {
        when(eventService.findById(eq(99L), any())).thenThrow(new EventNotFoundException(99L));

        assertThrows(EventNotFoundException.class,
                () -> rsvpService.upsertRsvp(99L, "bob", RsvpStatus.GOING));
    }

    // --- removeRsvp ---

    @Test
    public void removeRsvp_success_deletes() {
        when(eventService.findById(eq(1L), any())).thenReturn(event);
        when(rsvpRepository.existsByEventIdAndUsername(1L, "bob")).thenReturn(true);

        rsvpService.removeRsvp(1L, "bob");

        verify(rsvpRepository).deleteByEventIdAndUsername(1L, "bob");
    }

    @Test
    public void removeRsvp_noExistingRsvp_throwsIllegalState() {
        when(eventService.findById(eq(1L), any())).thenReturn(event);
        when(rsvpRepository.existsByEventIdAndUsername(1L, "bob")).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> rsvpService.removeRsvp(1L, "bob"));
        verify(rsvpRepository, never()).deleteByEventIdAndUsername(any(), any());
    }

    @Test
    public void removeRsvp_eventNotFound_throwsEventNotFound() {
        when(eventService.findById(eq(99L), any())).thenThrow(new EventNotFoundException(99L));

        assertThrows(EventNotFoundException.class, () -> rsvpService.removeRsvp(99L, "bob"));
    }

    // --- getSummary ---

    @Test
    public void getSummary_returnsCorrectCounts() {
        when(eventService.findById(eq(1L), any())).thenReturn(event);
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.GOING)).thenReturn(5L);
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.INTERESTED)).thenReturn(10L);
        when(rsvpRepository.countByEventIdAndStatus(1L, RsvpStatus.NOT_GOING)).thenReturn(2L);

        RsvpSummaryResponse summary = rsvpService.getSummary(1L);

        assertEquals(5L, summary.getGoing());
        assertEquals(10L, summary.getInterested());
        assertEquals(2L, summary.getNotGoing());
    }

    @Test
    public void getSummary_eventNotFound_throwsEventNotFound() {
        when(eventService.findById(eq(99L), any())).thenThrow(new EventNotFoundException(99L));

        assertThrows(EventNotFoundException.class, () -> rsvpService.getSummary(99L));
    }
}
