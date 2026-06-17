package com.eventmaster.repository;

import com.eventmaster.model.EventInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventInviteRepository extends JpaRepository<EventInvite, Long> {

    List<EventInvite> findByEventId(Long eventId);

    boolean existsByEventIdAndInviteeUsername(Long eventId, String inviteeUsername);

    void deleteByEventIdAndInviteeUsername(Long eventId, String inviteeUsername);

    void deleteByEventId(Long eventId);

    @Query("SELECT i.event.id FROM EventInvite i WHERE i.inviteeUsername = :username")
    List<Long> findEventIdsByInviteeUsername(@Param("username") String username);
}
