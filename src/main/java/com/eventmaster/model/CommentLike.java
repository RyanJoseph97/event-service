package com.eventmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"comment_id", "username"})
})
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(nullable = false)
    private String username;

    @Column(name = "liked_at", nullable = false)
    private LocalDateTime likedAt;

    public CommentLike() {}

    public CommentLike(Comment comment, String username) {
        this.comment = comment;
        this.username = username;
        this.likedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Comment getComment() { return comment; }
    public String getUsername() { return username; }
    public LocalDateTime getLikedAt() { return likedAt; }
}
