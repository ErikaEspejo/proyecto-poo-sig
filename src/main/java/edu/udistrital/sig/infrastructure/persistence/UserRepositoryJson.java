package edu.udistrital.sig.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import edu.udistrital.sig.domain.model.Role;
import edu.udistrital.sig.domain.model.User;
import edu.udistrital.sig.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryJson implements UserRepository {

    private static final String FILE_NAME = "users.json";

    private final JsonDataStore store;

    public UserRepositoryJson(JsonDataStore store) {
        this.store = store;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        for (JsonNode node : store.read(FILE_NAME)) {
            if (username.equals(node.path("username").asText())) {
                return Optional.of(new User(
                        node.path("username").asText(),
                        node.path("passwordHash").asText(),
                        Role.valueOf(node.path("role").asText())));
            }
        }
        return Optional.empty();
    }
}
