package com.eventmaster.controller;

import com.eventmaster.exception.EventNotFoundException;
import com.eventmaster.exception.GlobalExceptionHandler;
import com.eventmaster.model.*;
import com.eventmaster.service.LikeService;
import com.eventmaster.service.RsvpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LikeRsvpControllerTest {

    @Mock private LikeService likeService;
    @Mock private RsvpService rsvpService;

    @InjectMocks private LikeController likeController;
    @InjectMocks private RsvpController rsvpController;

    private MockMvc likeMvc;
    private MockMvc rsvpMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        likeMvc = MockMvcBuilders.standaloneSetup(likeController)
                .setControllerAdvice(handler).build();
        rsvpMvc = MockMvcBuilders.standaloneSetup(rsvpController)
                .setControllerAdvice(handler).build();
    }

    private RequestPostProcessor auth(String username) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("VERIFIED")));
        return (MockHttpServletRequest req) -> { req.setUserPrincipal(token); return req; };
    }

    // ─── Likes ───────────────────────────────────────────────────────────────

    @Test
    public void like_success_returns200() throws Exception {
        doNothing().when(likeService).like(1L, "bob");

        likeMvc.perform(post("/events/1/like").with(auth("bob")))
                .andExpect(status().isOk());

        verify(likeService).like(1L, "bob");
    }

    @Test
    public void like_alreadyLiked_returns409() throws Exception {
        doThrow(new IllegalStateException("You have already liked this event"))
                .when(likeService).like(1L, "bob");

        likeMvc.perform(post("/events/1/like").with(auth("bob")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You have already liked this event"));
    }

    @Test
    public void like_eventNotFound_returns404() throws Exception {
        doThrow(new EventNotFoundException(99L)).when(likeService).like(99L, "bob");

        likeMvc.perform(post("/events/99/like").with(auth("bob")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void unlike_success_returns204() throws Exception {
        doNothing().when(likeService).unlike(1L, "bob");

        likeMvc.perform(delete("/events/1/like").with(auth("bob")))
                .andExpect(status().isNoContent());
    }

    @Test
    public void unlike_notLiked_returns409() throws Exception {
        doThrow(new IllegalStateException("You have not liked this event"))
                .when(likeService).unlike(1L, "bob");

        likeMvc.perform(delete("/events/1/like").with(auth("bob")))
                .andExpect(status().isConflict());
    }

    @Test
    public void getLikeCount_returns200WithCount() throws Exception {
        when(likeService.getLikeCount(1L)).thenReturn(new LikeCountResponse(7L));

        likeMvc.perform(get("/events/1/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(7));
    }

    // ─── RSVP ────────────────────────────────────────────────────────────────

    @Test
    public void upsertRsvp_success_returns200() throws Exception {
        Event event = new Event("Music Night", "Live bands", "6th Street",
                LocalDateTime.of(2025, 6, 1, 20, 0), null, 100, "alice", Visibility.PUBLIC);
        ReflectionTestUtils.setField(event, "id", 1L);
        EventRsvp rsvp = new EventRsvp(event, "bob", RsvpStatus.GOING);

        when(rsvpService.upsertRsvp(1L, "bob", RsvpStatus.GOING)).thenReturn(rsvp);

        rsvpMvc.perform(post("/events/1/rsvp")
                        .with(auth("bob"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"GOING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GOING"))
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    public void upsertRsvp_missingStatus_returns400() throws Exception {
        rsvpMvc.perform(post("/events/1/rsvp")
                        .with(auth("bob"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists());
    }

    @Test
    public void upsertRsvp_eventNotFound_returns404() throws Exception {
        when(rsvpService.upsertRsvp(99L, "bob", RsvpStatus.GOING))
                .thenThrow(new EventNotFoundException(99L));

        rsvpMvc.perform(post("/events/99/rsvp")
                        .with(auth("bob"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"GOING\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void removeRsvp_success_returns204() throws Exception {
        doNothing().when(rsvpService).removeRsvp(1L, "bob");

        rsvpMvc.perform(delete("/events/1/rsvp").with(auth("bob")))
                .andExpect(status().isNoContent());
    }

    @Test
    public void removeRsvp_noRsvp_returns409() throws Exception {
        doThrow(new IllegalStateException("You do not have an RSVP for this event"))
                .when(rsvpService).removeRsvp(1L, "bob");

        rsvpMvc.perform(delete("/events/1/rsvp").with(auth("bob")))
                .andExpect(status().isConflict());
    }

    @Test
    public void getRsvpSummary_returns200WithCounts() throws Exception {
        when(rsvpService.getSummary(1L)).thenReturn(new RsvpSummaryResponse(5L, 10L, 2L));

        rsvpMvc.perform(get("/events/1/rsvps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.going").value(5))
                .andExpect(jsonPath("$.interested").value(10))
                .andExpect(jsonPath("$.notGoing").value(2));
    }
}
