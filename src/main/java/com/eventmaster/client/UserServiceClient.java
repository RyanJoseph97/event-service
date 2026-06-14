package com.eventmaster.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${user.service.base-url}")
    private String userServiceBaseUrl;

    public String getProfilePictureUrl(String username) {
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
