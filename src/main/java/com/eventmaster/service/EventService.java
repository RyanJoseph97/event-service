package com.eventmaster.service;

import com.eventmaster.model.Event;
import com.eventmaster.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    public Event saveUser(Event event) {
        return eventRepository.save(event);
    }

    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public Event findByTitle(String eventname){
        return eventRepository.findByTitle(eventname)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }


    public Event findById(Long id){
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}
