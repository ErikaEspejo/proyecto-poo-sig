package edu.udistrital.sig.infrastructure.web;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.udistrital.sig.domain.exception.AuthenticationRequiredException;
import edu.udistrital.sig.domain.exception.EntityNotFoundException;
import edu.udistrital.sig.domain.exception.InvalidCredentialsException;
import edu.udistrital.sig.domain.exception.InvalidEntityException;
import edu.udistrital.sig.domain.exception.InvalidGeometryException;
import edu.udistrital.sig.domain.exception.UnauthorizedOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({InvalidEntityException.class, InvalidGeometryException.class})
    public ResponseEntity<ObjectNode> badRequest(RuntimeException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({InvalidCredentialsException.class, AuthenticationRequiredException.class})
    public ResponseEntity<ObjectNode> unauthorized(RuntimeException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ObjectNode> forbidden(RuntimeException e) {
        return error(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ObjectNode> notFound(RuntimeException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ObjectNode> unreadable(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "Solicitud inválida.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ObjectNode> noResource(NoResourceFoundException e) {
        return error(HttpStatus.NOT_FOUND, "Recurso no encontrado.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ObjectNode> internal(Exception e) {
        log.error("Internal error", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno.");
    }

    private ResponseEntity<ObjectNode> error(HttpStatus status, String message) {
        ObjectNode body = JsonNodeFactory.instance.objectNode().put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}
