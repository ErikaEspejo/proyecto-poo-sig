package edu.udistrital.sig.domain.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String id) {
        super("La entidad " + id + " no existe.");
    }
}
