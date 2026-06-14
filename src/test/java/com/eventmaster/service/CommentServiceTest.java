package com.eventmaster.service;

import com.eventmaster.client.UserServiceClient;
import com.eventmaster.exception.CommentNotFoundException;
import com.eventmaster.exception.EventNotFoundException;
import com.eventmaster.model.Comment;
import com.eventmaster.model.CommentLike;
import com.eventmaster.model.CommentResponse;
import com.eventmaster.model.Event;
import com.eventmaster.model.Visibility;
import com.eventmaster.repository.CommentLikeRepository;
import com.eventmaster.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private EventService eventService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private CommentService commentService;

    private Event event;
    private Comment comment;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        event = new Event("Music Night", "Live bands", "6th Street",
                LocalDateTime.of(2025, 6, 1, 20, 0), null, 100, "alice", Visibility.PUBLIC);
        ReflectionTestUtils.setField(event, "id", 1L);

        comment = new Comment(1L, "bob", "Great event!");
        ReflectionTestUtils.setField(comment, "id", 10L);
    }

    // --- createComment ---

    @Test
    public void createComment_validEventAndUser_savesComment() {
        when(eventService.findById(1L)).thenReturn(event);
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        Comment result = commentService.createComment(1L, "Great event!", "bob");

        assertNotNull(result);
        assertEquals("bob", result.getUsername());
        assertEquals("Great event!", result.getContent());
        assertEquals(1L, result.getEventId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    public void createComment_eventNotFound_throwsEventNotFound() {
        when(eventService.findById(99L)).thenThrow(new EventNotFoundException(99L));

        assertThrows(EventNotFoundException.class,
                () -> commentService.createComment(99L, "Hello", "bob"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    public void createComment_setsCreatedAt() {
        when(eventService.findById(1L)).thenReturn(event);
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        Comment result = commentService.createComment(1L, "content", "alice");

        assertNotNull(result.getCreatedAt());
    }

    // --- getCommentsByEventId ---

    @Test
    public void getCommentsByEventId_returnsCommentList() {
        when(eventService.findById(1L)).thenReturn(event);
        when(commentRepository.findByEventId(1L)).thenReturn(List.of(comment));
        when(userServiceClient.getProfilePictureUrl("bob")).thenReturn("https://example.com/bob.jpg");

        List<CommentResponse> result = commentService.getCommentsByEventId(1L);

        assertEquals(1, result.size());
        assertEquals("bob", result.get(0).getUsername());
        assertEquals("https://example.com/bob.jpg", result.get(0).getProfilePictureUrl());
    }

    @Test
    public void getCommentsByEventId_noComments_returnsEmptyList() {
        when(eventService.findById(1L)).thenReturn(event);
        when(commentRepository.findByEventId(1L)).thenReturn(List.of());

        assertTrue(commentService.getCommentsByEventId(1L).isEmpty());
    }

    @Test
    public void getCommentsByEventId_eventNotFound_throwsEventNotFound() {
        when(eventService.findById(99L)).thenThrow(new EventNotFoundException(99L));

        assertThrows(EventNotFoundException.class,
                () -> commentService.getCommentsByEventId(99L));
    }

    // --- findById ---

    @Test
    public void findById_found_returnsComment() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        Comment result = commentService.findById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("bob", result.getUsername());
    }

    @Test
    public void findById_notFound_throwsCommentNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.findById(99L));
    }

    // --- likeComment ---

    @Test
    public void likeComment_success_savesCommentLike() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(commentLikeRepository.existsByCommentIdAndUsername(10L, "alice")).thenReturn(false);

        commentService.likeComment(10L, "alice");

        verify(commentLikeRepository).save(any(CommentLike.class));
    }

    @Test
    public void likeComment_alreadyLiked_throwsIllegalState() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(commentLikeRepository.existsByCommentIdAndUsername(10L, "alice")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> commentService.likeComment(10L, "alice"));
        verify(commentLikeRepository, never()).save(any());
    }

    @Test
    public void likeComment_commentNotFound_throwsCommentNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.likeComment(99L, "alice"));
    }

    // --- unlikeComment ---

    @Test
    public void unlikeComment_success_deletesLike() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(commentLikeRepository.existsByCommentIdAndUsername(10L, "alice")).thenReturn(true);

        commentService.unlikeComment(10L, "alice");

        verify(commentLikeRepository).deleteByCommentIdAndUsername(10L, "alice");
    }

    @Test
    public void unlikeComment_notLiked_throwsIllegalState() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(commentLikeRepository.existsByCommentIdAndUsername(10L, "alice")).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> commentService.unlikeComment(10L, "alice"));
        verify(commentLikeRepository, never()).deleteByCommentIdAndUsername(any(), any());
    }

    @Test
    public void unlikeComment_commentNotFound_throwsCommentNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.unlikeComment(99L, "alice"));
    }
}
