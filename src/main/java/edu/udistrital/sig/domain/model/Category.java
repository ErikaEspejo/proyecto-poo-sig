package edu.udistrital.sig.domain.model;

import edu.udistrital.sig.domain.exception.InvalidEntityException;

public record Category(String id, String name) {

    public Category {
        if (id == null || id.isBlank()) {
            throw new InvalidEntityException("El identificador de la categoría es obligatorio.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidEntityException("El nombre de la categoría es obligatorio.");
        }
    }
}
