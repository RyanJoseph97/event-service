package com.eventmaster.service;

import com.eventmaster.client.UserServiceClient;
import com.eventmaster.exception.EventNotFoundException;
import com.eventmaster.model.Event;
import com.eventmaster.model.EventLike;
import com.eventmaster.model.LikeCountResponse;
import com.eventmaster.model.Visibility;
import com.eventmaster.repository.EventLikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class LikeServiceTest {

    @Mock
    private EventLikeRepository likeRepository;

    @Mock
    private EventService eventService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private LikeService likeService;

    private Event event;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        event = new Event("Music Night", "Live bands", "6th Street",
                LocalDateTime.of(2025, 6, 1, 20, 0), null, 100, "alice", Visibility.PUBLIC);
        ReflectionTestUtils.setField(event, "id", 1L);
    }

    // --- like ---

    @Test
    public void like_success_savesLike() {
        when(eventService.findById(eq(1L), any())).thenReturn(event);
        when(likeRepository.existsByEventIdAndUsername(1L, "bob")).thenReturn(false);

        likeService.like(1L, "bob");

        verify(likeRepository).save(any(EventLike.class));
    }

    @Test
    public void like_alreadyLiked_throwsIllegalState() {
        when(eventService.findById(eq(1L), any())).thenReturn(event);
        when(likeRepository.existsByEventIdAndUsername(1L, "bob")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> likeService.like(1L, "bob"));
        verify(likeRepository, never()).save(any());
    }

    @Test
    public void like_eventNotFound_throwsEventNotFound() {
        when(eventService.findById(eq(99L), any())).thenThrow(new EventNotFoundException(99L));

        assertThrows(EventNotFoundException.class, () -> likeService.like(99L, "bob"));
    }

    // --- unlike ---

    @Test
    public void unlike_success_deletesLike() {
        when(eventService.findById(eq(1L), any())).thenReturn(event);
        when(likeRepository.existsByEventIdAndUsername(1L, "bob")).thenReturn(true);

        likeService.unlike(1L, "bob");

        verify(likeRepository).deleteByEventIdAndUsername(1L, "bob");
    }

    @Test
    public void unlike_notLiked_throwsIllegalState() {
        when(eventService.findById(eq(1L), any())).thenReturn(event);
        when(likeRepository.existsByEventIdAndUsername(1L, "bob")).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> likeService.unlike(1L, "bob"));
        verify(likeRepository, never()).deleteByEventIdAndUsername(any(), any());
    }

    @Test
    public void unlike_eventNotFound_throwsEventNotFound() {
        when(eventService.findById(eq(99L), any())).thenThrow(new EventNotFoundException(99L));

        assertThrows(EventNotFoundException.class, () -> likeService.unlike(99L, "bob"));
    }

    // --- getLikeCount ---

    @Test
    public void getLikeCount_returnsCount() {
        when(eventService.findById(eq(1L), any())).thenReturn(event);
        when(likeRepository.countByEventId(1L)).thenReturn(7L);

        LikeCountResponse response = likeService.getLikeCount(1L);

        assertEquals(7L, response.getCount());
    }

    @Test
    public void getLikeCount_eventNotFound_throwsEventNotFound() {
        when(eventService.findById(eq(99L), any())).thenThrow(new EventNotFoundException(99L));

        assertThrows(EventNotFoundException.class, () -> likeService.getLikeCount(99L));
    }
}
