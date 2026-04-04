package com.eventmaster.repository;

import com.eventmaster.model.EventLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLikeRepository extends JpaRepository<EventLike, Long> {

    boolean existsByEventIdAndUsername(Long eventId, String username);

    long countByEventId(Long eventId);

    void deleteByEventIdAndUsername(Long eventId, String username);
}
