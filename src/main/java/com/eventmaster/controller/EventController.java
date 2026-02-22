package com.eventmaster.controller;

import com.eventmaster.model.Event;
import com.eventmaster.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    @Autowired
    public EventController(EventService EventService) {
        this.eventService = EventService;
    }

    @GetMapping("/title")
    public ResponseEntity<Event> getUserByTitle(@PathVariable String title){
        Event event = eventService.findByTitle(title);
        if (event != null){
            return ResponseEntity.ok(event);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventbyId(Long id) {
        Event event = eventService.findById(id);
        if (event != null){
            return ResponseEntity.ok(event);
        } else{
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        events.forEach(System.out::println); // or use a logger
        return ResponseEntity.ok(events);
    }
    

    @PostMapping
    public Event createUser(@RequestBody Event event){
        return eventService.saveUser(event);
    }


}
