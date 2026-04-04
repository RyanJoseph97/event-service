package com.eventmaster.service;

import com.eventmaster.model.Event;
import com.eventmaster.model.EventRsvp;
import com.eventmaster.model.RsvpStatus;
import com.eventmaster.model.RsvpSummaryResponse;
import com.eventmaster.repository.EventRsvpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RsvpService {

    private static final Logger logger = LoggerFactory.getLogger(RsvpService.class);

    @Autowired
    private EventRsvpRepository rsvpRepository;

    @Autowired
    private EventService eventService;

    /**
     * Creates or updates an RSVP. If the user already has an RSVP for the event
     * the status is updated in place (upsert semantics).
     */
    @Transactional
    public EventRsvp upsertRsvp(Long eventId, String username, RsvpStatus status) {
        Event event = eventService.findById(eventId);
        try {
            // Use map() so setStatus() (which stamps updatedAt) only runs on existing RSVPs,
            // not on a freshly constructed one where updatedAt should remain null.
            EventRsvp rsvp = rsvpRepository.findByEventIdAndUsername(eventId, username)
                    .map(existing -> { existing.setStatus(status); return existing; })
                    .orElse(new EventRsvp(event, username, status));
            EventRsvp saved = rsvpRepository.save(rsvp);
            logger.info("User '{}' RSVPed to event {} with status {}", username, eventId, status);
            return saved;
        } catch (DataIntegrityViolationException e) {
            // Race condition: a concurrent request inserted the row between our find and save.
            // Retry as a guaranteed update on the now-existing row.
            EventRsvp existing = rsvpRepository.findByEventIdAndUsername(eventId, username)
                    .orElseThrow(() -> new RuntimeException("RSVP upsert failed unexpectedly", e));
            existing.setStatus(status);
            return rsvpRepository.save(existing);
        }
    }

    @Transactional
    public void removeRsvp(Long eventId, String username) {
        eventService.findById(eventId); // verify event exists
        if (!rsvpRepository.existsByEventIdAndUsername(eventId, username)) {
            throw new IllegalStateException("You do not have an RSVP for this event");
        }
        rsvpRepository.deleteByEventIdAndUsername(eventId, username);
        logger.info("User '{}' removed RSVP from event {}", username, eventId);
    }

    public RsvpSummaryResponse getSummary(Long eventId) {
        eventService.findById(eventId); // verify event exists
        long going      = rsvpRepository.countByEventIdAndStatus(eventId, RsvpStatus.GOING);
        long interested = rsvpRepository.countByEventIdAndStatus(eventId, RsvpStatus.INTERESTED);
        long notGoing   = rsvpRepository.countByEventIdAndStatus(eventId, RsvpStatus.NOT_GOING);
        return new RsvpSummaryResponse(going, interested, notGoing);
    }
}
