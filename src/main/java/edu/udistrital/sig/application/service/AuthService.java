package edu.udistrital.sig.application.service;

import edu.udistrital.sig.domain.exception.AuthenticationRequiredException;
import edu.udistrital.sig.domain.exception.InvalidCredentialsException;
import edu.udistrital.sig.domain.model.User;
import edu.udistrital.sig.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenManager tokenManager;
    private final PasswordHasher passwordHasher;

    public AuthService(UserRepository userRepository, TokenManager tokenManager, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.tokenManager = tokenManager;
        this.passwordHasher = passwordHasher;
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .filter(stored -> stored.passwordHash().equals(passwordHasher.hash(password)))
                .orElseThrow(InvalidCredentialsException::new);
        return tokenManager.issue(user);
    }

    public User resolve(String token) {
        return tokenManager.resolve(token)
                .orElseThrow(AuthenticationRequiredException::new);
    }
}
