package com.eventmaster.repository;

import com.eventmaster.model.Event;
import com.eventmaster.model.Visibility;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecification {

    public static Specification<Event> locationContains(String location) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    public static Specification<Event> creatorUsernameEquals(String creatorUsername) {
        return (root, query, cb) ->
                cb.equal(root.get("creatorUsername"), creatorUsername);
    }

    public static Specification<Event> startAfter(LocalDateTime startAfter) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("startTime"), startAfter);
    }

    public static Specification<Event> startBefore(LocalDateTime startBefore) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("startTime"), startBefore);
    }

    public static Specification<Event> visibilityEquals(Visibility visibility) {
        return (root, query, cb) ->
                cb.equal(root.get("visibility"), visibility);
    }

    public static Specification<Event> creatorUsernameIn(List<String> usernames) {
        return (root, query, cb) ->
                root.get("creatorUsername").in(usernames);
    }

    public static Specification<Event> visibleTo(String viewerUsername, List<Long> invitedEventIds) {
        return (root, query, cb) -> {
            List<Predicate> or = new ArrayList<>();
            or.add(cb.equal(root.get("visibility"), Visibility.PUBLIC));
            or.add(cb.equal(root.get("creatorUsername"), viewerUsername));
            if (!invitedEventIds.isEmpty()) {
                or.add(root.get("id").in(invitedEventIds));
            }
            return cb.or(or.toArray(new Predicate[0]));
        };
    }
}
