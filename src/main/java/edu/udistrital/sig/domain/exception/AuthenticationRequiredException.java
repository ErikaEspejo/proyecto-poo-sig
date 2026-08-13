package edu.udistrital.sig.domain.exception;

public class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException() {
        super("Debe iniciar sesión.");
    }
}
