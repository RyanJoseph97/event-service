package com.eventmaster.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    public void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    public void handleEventNotFound_returns404() {
        EventNotFoundException ex = new EventNotFoundException(42L);

        ResponseEntity<Object> response = handler.handleEventNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertTrue(body.get("message").toString().contains("42"));
    }

    @Test
    public void handleForbidden_returns403() {
        ForbiddenException ex = new ForbiddenException("Only verified users can create public events");

        ResponseEntity<Object> response = handler.handleForbidden(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(403, body.get("status"));
        assertEquals("Forbidden", body.get("error"));
        assertEquals("Only verified users can create public events", body.get("message"));
    }

    @Test
    public void handleIllegalState_returns409() {
        IllegalStateException ex = new IllegalStateException("Already liked");

        ResponseEntity<Object> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(409, body.get("status"));
        assertEquals("Conflict", body.get("error"));
        assertEquals("Already liked", body.get("message"));
    }

    @Test
    public void handleIllegalArgument_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        ResponseEntity<Object> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Invalid input", body.get("message"));
    }

    @Test
    public void handleCommentNotFound_returns404() {
        CommentNotFoundException ex = new CommentNotFoundException(7L);

        ResponseEntity<Object> response = handler.handleCommentNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertTrue(body.get("message").toString().contains("7"));
    }

    @Test
    public void handleGlobal_returns500() {
        Exception ex = new RuntimeException("Something unexpected");

        ResponseEntity<Object> response = handler.handleGlobal(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
    }
}
