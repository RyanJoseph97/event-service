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
}
