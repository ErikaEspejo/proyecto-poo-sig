package edu.udistrital.sig.infrastructure.web;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.udistrital.sig.application.service.AuthService;
import edu.udistrital.sig.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ObjectNode> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.username(), request.password());
        User user = authService.resolve(token);
        ObjectNode body = JsonNodeFactory.instance.objectNode()
                .put("token", token)
                .put("role", user.role().name());
        return ResponseEntity.ok(body);
    }

    public record LoginRequest(String username, String password) {
    }
}
