package edu.udistrital.sig.domain.exception;

public class UnauthorizedOperationException extends RuntimeException {

    public UnauthorizedOperationException() {
        super("No tiene permisos para realizar esta operación.");
    }
}
