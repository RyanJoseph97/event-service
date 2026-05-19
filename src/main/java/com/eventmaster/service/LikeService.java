package com.eventmaster.service;

import com.eventmaster.model.Event;
import com.eventmaster.model.EventLike;
import com.eventmaster.model.LikeCountResponse;
import com.eventmaster.repository.EventLikeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private static final Logger logger = LoggerFactory.getLogger(LikeService.class);

    @Autowired
    private EventLikeRepository likeRepository;

    @Autowired
    private EventService eventService;

    @Transactional
    public void like(Long eventId, String username) {
        Event event = eventService.findById(eventId);
        if (likeRepository.existsByEventIdAndUsername(eventId, username)) {
            throw new IllegalStateException("You have already liked this event");
        }
        try {
            likeRepository.save(new EventLike(event, username));
        } catch (DataIntegrityViolationException e) {
            // Race condition: concurrent request inserted a like between our existence check and save.
            throw new IllegalStateException("You have already liked this event");
        }
        logger.info("User '{}' liked event {}", username, eventId);
    }

    @Transactional
    public void unlike(Long eventId, String username) {
        eventService.findById(eventId); // verify event exists
        if (!likeRepository.existsByEventIdAndUsername(eventId, username)) {
            throw new IllegalStateException("You have not liked this event");
        }
        likeRepository.deleteByEventIdAndUsername(eventId, username);
        logger.info("User '{}' unliked event {}", username, eventId);
    }

    public LikeCountResponse getLikeCount(Long eventId) {
        eventService.findById(eventId); // verify event exists
        long count = likeRepository.countByEventId(eventId);
        return new LikeCountResponse(count);
    }

    public boolean isLikedByUser(Long eventId, String username) {
        return likeRepository.existsByEventIdAndUsername(eventId, username);
    }
}
