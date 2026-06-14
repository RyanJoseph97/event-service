package com.eventmaster.repository;

import com.eventmaster.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findByCreatorUsername(String creatorUsername);

    // Full-text search using PostgreSQL GIN index on (title, description).
    // invitedIds must contain at least one element — pass [-1] when the list is empty.
    @Query(value =
            "SELECT e.* FROM events e" +
            " WHERE to_tsvector('english', coalesce(e.title,'') || ' ' || coalesce(e.description,''))" +
            "       @@ plainto_tsquery('english', :keyword)" +
            "   AND (:location IS NULL OR lower(e.location) LIKE lower('%' || :location || '%'))" +
            "   AND (:category IS NULL OR e.category = :category)" +
            "   AND (:startAfter IS NULL OR e.start_time >= :startAfter)" +
            "   AND (:startBefore IS NULL OR e.start_time <= :startBefore)" +
            "   AND (:creatorUsername IS NULL OR e.creator_username = :creatorUsername)" +
            "   AND (" +
            "     e.visibility = 'PUBLIC'" +
            "     OR (:viewerUsername IS NOT NULL AND e.creator_username = :viewerUsername)" +
            "     OR (:viewerUsername IS NOT NULL AND e.id IN (:invitedIds))" +
            "   )",
            countQuery =
            "SELECT count(*) FROM events e" +
            " WHERE to_tsvector('english', coalesce(e.title,'') || ' ' || coalesce(e.description,''))" +
            "       @@ plainto_tsquery('english', :keyword)" +
            "   AND (:location IS NULL OR lower(e.location) LIKE lower('%' || :location || '%'))" +
            "   AND (:category IS NULL OR e.category = :category)" +
            "   AND (:startAfter IS NULL OR e.start_time >= :startAfter)" +
            "   AND (:startBefore IS NULL OR e.start_time <= :startBefore)" +
            "   AND (:creatorUsername IS NULL OR e.creator_username = :creatorUsername)" +
            "   AND (" +
            "     e.visibility = 'PUBLIC'" +
            "     OR (:viewerUsername IS NOT NULL AND e.creator_username = :viewerUsername)" +
            "     OR (:viewerUsername IS NOT NULL AND e.id IN (:invitedIds))" +
            "   )",
            nativeQuery = true)
    Page<Event> fullTextSearch(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("category") String category,
            @Param("startAfter") LocalDateTime startAfter,
            @Param("startBefore") LocalDateTime startBefore,
            @Param("creatorUsername") String creatorUsername,
            @Param("viewerUsername") String viewerUsername,
            @Param("invitedIds") List<Long> invitedIds,
            Pageable pageable);
}
