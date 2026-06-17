package com.eventmaster.repository;

import com.eventmaster.model.EventLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EventLikeRepository extends JpaRepository<EventLike, Long> {

    boolean existsByEventIdAndUsername(Long eventId, String username);

    long countByEventId(Long eventId);

    void deleteByEventIdAndUsername(Long eventId, String username);

    void deleteByEventId(Long eventId);

    @Query("SELECT e.event.id, COUNT(e) FROM EventLike e WHERE e.event.id IN :ids GROUP BY e.event.id")
    List<Object[]> countGroupedByEventId(@Param("ids") Collection<Long> ids);
}
