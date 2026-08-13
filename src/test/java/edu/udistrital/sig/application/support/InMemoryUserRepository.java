package edu.udistrital.sig.application.support;

import edu.udistrital.sig.domain.model.User;
import edu.udistrital.sig.domain.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> users;

    public InMemoryUserRepository(Map<String, User> users) {
        this.users = users;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }
}
