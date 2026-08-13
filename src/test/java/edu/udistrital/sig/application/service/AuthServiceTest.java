package edu.udistrital.sig.application.service;

import edu.udistrital.sig.application.support.InMemoryUserRepository;
import edu.udistrital.sig.domain.exception.AuthenticationRequiredException;
import edu.udistrital.sig.domain.exception.InvalidCredentialsException;
import edu.udistrital.sig.domain.model.Role;
import edu.udistrital.sig.domain.model.User;
import edu.udistrital.sig.infrastructure.security.Sha256PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {

    private AuthService service;

    @BeforeEach
    void setUp() {
        InMemoryUserRepository users = new InMemoryUserRepository(Map.of(
                "admin", new User("admin", new Sha256PasswordHasher().hash("admin123"), Role.ADMINISTRATOR)));
        service = new AuthService(users, new TokenManager(), new Sha256PasswordHasher());
    }

    @Test
    void loginIssuesTokenAndResolvesUser() {
        String token = service.login("admin", "admin123");

        assertNotNull(token);
        assertEquals(Role.ADMINISTRATOR, service.resolve(token).role());
    }

    @Test
    void eachLoginIssuesADifferentToken() {
        String first = service.login("admin", "admin123");
        String second = service.login("admin", "admin123");

        assertNotEquals(first, second);
    }

    @Test
    void wrongPasswordIsRejected() {
        assertThrows(InvalidCredentialsException.class, () -> service.login("admin", "incorrecta"));
    }

    @Test
    void unknownUserIsRejected() {
        assertThrows(InvalidCredentialsException.class, () -> service.login("nadie", "admin123"));
    }

    @Test
    void unknownTokenIsRejected() {
        assertThrows(AuthenticationRequiredException.class, () -> service.resolve("token-invalido"));
    }
}
