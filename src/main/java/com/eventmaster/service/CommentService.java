package com.eventmaster.service;

import com.eventmaster.exception.CommentNotFoundException;
import com.eventmaster.model.Comment;
import com.eventmaster.model.CommentLike;
import com.eventmaster.repository.CommentLikeRepository;
import com.eventmaster.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private EventService eventService;

    @Transactional
    public Comment createComment(Long eventId, String content, String username) {
        eventService.findById(eventId);
        Comment comment = new Comment(eventId, username, content);
        Comment saved = commentRepository.save(comment);
        logger.info("User '{}' commented on event {}", username, eventId);
        return saved;
    }

    public List<Comment> getCommentsByEventId(Long eventId) {
        eventService.findById(eventId);
        return commentRepository.findByEventId(eventId);
    }

    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
    }

    @Transactional
    public void likeComment(Long commentId, String username) {
        Comment comment = findById(commentId);
        if (commentLikeRepository.existsByCommentIdAndUsername(commentId, username)) {
            throw new IllegalStateException("You have already liked this comment");
        }
        try {
            commentLikeRepository.save(new CommentLike(comment, username));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("You have already liked this comment");
        }
        logger.info("User '{}' liked comment {}", username, commentId);
    }

    @Transactional
    public void unlikeComment(Long commentId, String username) {
        findById(commentId);
        if (!commentLikeRepository.existsByCommentIdAndUsername(commentId, username)) {
            throw new IllegalStateException("You have not liked this comment");
        }
        commentLikeRepository.deleteByCommentIdAndUsername(commentId, username);
        logger.info("User '{}' unliked comment {}", username, commentId);
    }
}
