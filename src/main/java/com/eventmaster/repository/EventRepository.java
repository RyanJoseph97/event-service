package com.eventmaster.repository;
import com.eventmaster.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Find user by username
    Optional<Event> findByTitle(String title);

    // Find user by email
    Optional<Event> findByCreatorId(String creatorId);

    Optional<Event> findById(long id);

    // Make sure there is no custom query defined for findAll()
    List<Event> findAll();
}