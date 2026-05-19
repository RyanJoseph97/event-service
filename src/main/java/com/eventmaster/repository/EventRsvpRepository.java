package com.eventmaster.repository;

import com.eventmaster.model.EventRsvp;
import com.eventmaster.model.RsvpStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRsvpRepository extends JpaRepository<EventRsvp, Long> {

    Optional<EventRsvp> findByEventIdAndUsername(Long eventId, String username);

    boolean existsByEventIdAndUsername(Long eventId, String username);

    long countByEventIdAndStatus(Long eventId, RsvpStatus status);

    void deleteByEventIdAndUsername(Long eventId, String username);

    void deleteByEventId(Long eventId);
}
