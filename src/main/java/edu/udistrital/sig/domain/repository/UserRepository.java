package edu.udistrital.sig.domain.repository;

import edu.udistrital.sig.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);
}
