package com.eventmaster.repository;

import com.eventmaster.model.SavedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedEventRepository extends JpaRepository<SavedEvent, Long> {

    boolean existsByUsernameAndEventId(String username, Long eventId);

    List<SavedEvent> findByUsername(String username);

    void deleteByUsernameAndEventId(String username, Long eventId);

    void deleteByEventId(Long eventId);
}
