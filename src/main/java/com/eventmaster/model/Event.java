package com.eventmaster.model;

import javax.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name="start_time")
    private LocalDateTime startTime;

    @Column(name="end_time")
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String location;

    @Column(name = "creator_id")
    private long creatorId;

    // Constructors, getters, setters, etc.
    public Event(){};

    public Event(Long id, String title, String description, String location, LocalDateTime startTime, long creatorId){
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.creatorId = creatorId;
    }

    public Event(Long id, String title, String description, String location, LocalDateTime startTime, LocalDateTime endTime, long creatorId){
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.creatorId = creatorId;
        this.endTime = endTime;
    }

    public Long getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String Description(){
        return description;
    }

    public LocalDateTime getStartTime(){
        return startTime;
    }

    public String getLocation(){
        return location;
    }

    public LocalDateTime getEndTime(){ return endTime; }

    public long getCreatorId(){ return creatorId; }
}
