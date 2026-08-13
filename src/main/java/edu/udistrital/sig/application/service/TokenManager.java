package edu.udistrital.sig.application.service;

import edu.udistrital.sig.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenManager {

    private final Map<String, User> tokens = new ConcurrentHashMap<>();

    public String issue(User user) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, user);
        return token;
    }

    public Optional<User> resolve(String token) {
        return Optional.ofNullable(tokens.get(token));
    }
}
