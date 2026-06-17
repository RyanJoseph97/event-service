package com.eventmaster.repository;

import com.eventmaster.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByCommentIdAndUsername(Long commentId, String username);

    long countByCommentId(Long commentId);

    void deleteByCommentIdAndUsername(Long commentId, String username);

    void deleteByComment_EventId(Long eventId);
}
