package com.eventmaster.repository;

import com.eventmaster.model.EventRsvp;
import com.eventmaster.model.RsvpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRsvpRepository extends JpaRepository<EventRsvp, Long> {

    Optional<EventRsvp> findByEventIdAndUsername(Long eventId, String username);

    boolean existsByEventIdAndUsername(Long eventId, String username);

    long countByEventIdAndStatus(Long eventId, RsvpStatus status);

    void deleteByEventIdAndUsername(Long eventId, String username);

    void deleteByEventId(Long eventId);

    List<EventRsvp> findByUsernameAndStatusIn(String username, List<RsvpStatus> statuses);

    @Query("SELECT r FROM EventRsvp r JOIN FETCH r.event WHERE r.username = :username AND r.status IN :statuses")
    List<EventRsvp> findByUsernameAndStatusInWithEvent(@Param("username") String username, @Param("statuses") List<RsvpStatus> statuses);
}
