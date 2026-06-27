package com.eventmaster.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

    private final Cache<String, String> profilePictureCache;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user.service.base-url}")
    private String userServiceBaseUrl;

    public UserServiceClient(
            @Value("${user.service.profile-picture-cache-ttl-minutes:60}") int cacheTtlMinutes) {
        this.profilePictureCache = Caffeine.newBuilder()
                .expireAfterWrite(cacheTtlMinutes, TimeUnit.MINUTES)
                .build();
    }

    public String getProfilePictureUrl(String username) {
        return profilePictureCache.get(username, this::fetchProfilePictureUrl);
    }

    private String fetchProfilePictureUrl(String username) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> user = restTemplate.getForObject(
                    userServiceBaseUrl + "/users/" + username, Map.class);
            if (user != null) {
                Object url = user.get("profilePictureUrl");
                return url instanceof String ? (String) url : null;
            }
        } catch (RestClientException e) {
            logger.debug("Could not fetch profile picture for {}: {}", username, e.getMessage());
        }
        return null;
    }

    /**
     * Create a notification for a user via user-service's network-internal endpoint.
     * Best-effort: never fails the caller — the triggering action (invite, like,
     * comment, RSVP) should succeed even if the notification can't be written.
     *
     * @param type one of the user-service NotificationType values, e.g. "EVENT_INVITE"
     * @param actorUsername the user who triggered it
     * @param entityId related entity reference (event id, username), nullable
     */
    public void sendNotification(String recipientUsername, String type, String actorUsername,
                                 String entityId, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("recipientUsername", recipientUsername);
        body.put("type", type);
        body.put("actorUsername", actorUsername);
        body.put("entityId", entityId);
        body.put("message", message);
        try {
            restTemplate.postForObject(userServiceBaseUrl + "/internal/notifications", body, Void.class);
        } catch (RestClientException e) {
            logger.warn("Could not send {} notification to {}: {}", type, recipientUsername, e.getMessage());
        }
    }
}
