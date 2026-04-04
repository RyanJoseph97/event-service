package com.eventmaster.controller;

import com.eventmaster.exception.EventNotFoundException;
import com.eventmaster.exception.ForbiddenException;
import com.eventmaster.exception.GlobalExceptionHandler;
import com.eventmaster.model.CreateEventRequest;
import com.eventmaster.model.Event;
import com.eventmaster.model.UpdateEventRequest;
import com.eventmaster.model.Visibility;
import com.eventmaster.service.EventService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Note: getAllEvents now takes (location, creatorUsername, startAfter, startBefore, visibility)

public class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;
    private Event sampleEvent;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(eventController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleEvent = new Event("Music Night", "Live bands", "6th Street",
                LocalDateTime.of(2025, 6, 1, 20, 0), null, 100, "alice", Visibility.PUBLIC);
        ReflectionTestUtils.setField(sampleEvent, "id", 1L);
    }

    /** Sets request principal so Spring MVC injects Authentication into controller params. */
    private RequestPostProcessor auth(String username, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
        return (MockHttpServletRequest request) -> {
            request.setUserPrincipal(token);
            return request;
        };
    }

    // --- GET /{id} ---

    @Test
    public void getEventById_found_returns200() throws Exception {
        when(eventService.findById(1L)).thenReturn(sampleEvent);

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Music Night"));
    }

    @Test
    public void getEventById_notFound_returns404() throws Exception {
        when(eventService.findById(99L)).thenThrow(new EventNotFoundException(99L));

        mockMvc.perform(get("/events/99"))
                .andExpect(status().isNotFound());
    }

    // --- GET ---

    @Test
    public void getAllEvents_noParams_returns200WithList() throws Exception {
        when(eventService.getAllEvents(null, null, null, null, null)).thenReturn(List.of(sampleEvent));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Music Night"));
    }

    @Test
    public void getAllEvents_withLocationFilter_passesParamToService() throws Exception {
        when(eventService.getAllEvents(eq("Austin"), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(sampleEvent));

        mockMvc.perform(get("/events").param("location", "Austin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Music Night"));
    }

    @Test
    public void getAllEvents_withVisibilityFilter_passesParamToService() throws Exception {
        when(eventService.getAllEvents(isNull(), isNull(), isNull(), isNull(), eq(Visibility.PUBLIC)))
                .thenReturn(List.of(sampleEvent));

        mockMvc.perform(get("/events").param("visibility", "PUBLIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Music Night"));
    }

    // --- GET /by-creator/{username} ---

    @Test
    public void getEventsByCreator_returns200() throws Exception {
        when(eventService.findByCreatorUsername("alice")).thenReturn(List.of(sampleEvent));

        mockMvc.perform(get("/events/by-creator/alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].creatorUsername").value("alice"));
    }

    // --- POST ---

    @Test
    public void createPublicEvent_asVerifiedUser_returns200() throws Exception {
        when(eventService.createEvent(any(CreateEventRequest.class), eq("alice")))
                .thenReturn(sampleEvent);

        String body = "{\"title\":\"Music Night\",\"description\":\"Live bands\",\"location\":\"6th Street\","
                + "\"startTime\":\"2025-06-01T20:00:00\",\"visibility\":\"PUBLIC\"}";

        mockMvc.perform(post("/events")
                        .with(auth("alice", "VERIFIED"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Music Night"));
    }

    @Test
    public void createPublicEvent_asTrustedUser_returns200() throws Exception {
        when(eventService.createEvent(any(CreateEventRequest.class), eq("admin")))
                .thenReturn(sampleEvent);

        String body = "{\"title\":\"Music Night\",\"description\":\"Live bands\",\"location\":\"6th Street\","
                + "\"startTime\":\"2025-06-01T20:00:00\",\"visibility\":\"PUBLIC\"}";

        mockMvc.perform(post("/events")
                        .with(auth("admin", "TRUSTED"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    public void createPublicEvent_asUnverifiedUser_returns403() throws Exception {
        String body = "{\"title\":\"Music Night\",\"description\":\"Live bands\",\"location\":\"6th Street\","
                + "\"startTime\":\"2025-06-01T20:00:00\",\"visibility\":\"PUBLIC\"}";

        mockMvc.perform(post("/events")
                        .with(auth("newuser", "UNVERIFIED"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(eventService, never()).createEvent(any(), any());
    }

    @Test
    public void createInviteOnlyEvent_asUnverifiedUser_returns200() throws Exception {
        Event privateEvent = new Event("Private Party", "Invite only", "Home",
                LocalDateTime.of(2025, 6, 1, 18, 0), null, 10, "newuser", Visibility.INVITE_ONLY);
        when(eventService.createEvent(any(CreateEventRequest.class), eq("newuser")))
                .thenReturn(privateEvent);

        String body = "{\"title\":\"Private Party\",\"description\":\"Invite only\",\"location\":\"Home\","
                + "\"startTime\":\"2025-06-01T18:00:00\",\"visibility\":\"INVITE_ONLY\"}";

        mockMvc.perform(post("/events")
                        .with(auth("newuser", "UNVERIFIED"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    public void createEvent_missingTitle_returns400() throws Exception {
        String body = "{\"description\":\"No title\",\"location\":\"Somewhere\","
                + "\"startTime\":\"2025-06-01T20:00:00\"}";

        mockMvc.perform(post("/events")
                        .with(auth("alice", "VERIFIED"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    public void createEvent_missingStartTime_returns400() throws Exception {
        String body = "{\"title\":\"No Start\",\"description\":\"Missing time\",\"location\":\"Somewhere\"}";

        mockMvc.perform(post("/events")
                        .with(auth("alice", "VERIFIED"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.startTime").exists());
    }

    @Test
    public void createEvent_withImageUrl_returnsImageUrlInResponse() throws Exception {
        Event eventWithImage = new Event("Music Night", "Live bands", "6th Street",
                LocalDateTime.of(2025, 6, 1, 20, 0), null, 100, "alice", Visibility.PUBLIC);
        ReflectionTestUtils.setField(eventWithImage, "id", 2L);
        eventWithImage.setImageUrl("https://example.com/image.jpg");

        when(eventService.createEvent(any(CreateEventRequest.class), eq("alice")))
                .thenReturn(eventWithImage);

        String body = "{\"title\":\"Music Night\",\"description\":\"Live bands\",\"location\":\"6th Street\","
                + "\"startTime\":\"2025-06-01T20:00:00\",\"visibility\":\"PUBLIC\","
                + "\"imageUrl\":\"https://example.com/image.jpg\"}";

        mockMvc.perform(post("/events")
                        .with(auth("alice", "VERIFIED"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/image.jpg"));
    }

    // --- PATCH /{id} ---

    @Test
    public void updateEvent_asOwner_returns200() throws Exception {
        when(eventService.updateEvent(eq(1L), any(UpdateEventRequest.class), eq("alice")))
                .thenReturn(sampleEvent);

        mockMvc.perform(patch("/events/1")
                        .with(auth("alice", "VERIFIED"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Title\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void updateEvent_asNonOwner_returns403() throws Exception {
        when(eventService.updateEvent(eq(1L), any(UpdateEventRequest.class), eq("bob")))
                .thenThrow(new ForbiddenException("You do not have permission to modify this event"));

        mockMvc.perform(patch("/events/1")
                        .with(auth("bob", "VERIFIED"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hijacked\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void updateEvent_notFound_returns404() throws Exception {
        when(eventService.updateEvent(eq(99L), any(), any()))
                .thenThrow(new EventNotFoundException(99L));

        mockMvc.perform(patch("/events/99")
                        .with(auth("alice", "VERIFIED"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Ghost\"}"))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /{id} ---

    @Test
    public void deleteEvent_asOwner_returns204() throws Exception {
        doNothing().when(eventService).deleteEvent(1L, "alice");

        mockMvc.perform(delete("/events/1").with(auth("alice", "VERIFIED")))
                .andExpect(status().isNoContent());

        verify(eventService).deleteEvent(1L, "alice");
    }

    @Test
    public void deleteEvent_asNonOwner_returns403() throws Exception {
        doThrow(new ForbiddenException("You do not have permission to delete this event"))
                .when(eventService).deleteEvent(1L, "bob");

        mockMvc.perform(delete("/events/1").with(auth("bob", "VERIFIED")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteEvent_notFound_returns404() throws Exception {
        doThrow(new EventNotFoundException(99L)).when(eventService).deleteEvent(99L, "alice");

        mockMvc.perform(delete("/events/99").with(auth("alice", "VERIFIED")))
                .andExpect(status().isNotFound());
    }
}
