package edu.udistrital.sig.domain.model;

import edu.udistrital.sig.domain.exception.InvalidEntityException;

public record User(String username, String passwordHash, Role role) {

    public User {
        if (username == null || username.isBlank()) {
            throw new InvalidEntityException("El nombre de usuario es obligatorio.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidEntityException("La contraseña del usuario es obligatoria.");
        }
        if (role == null) {
            throw new InvalidEntityException("El rol del usuario es obligatorio.");
        }
    }
}
