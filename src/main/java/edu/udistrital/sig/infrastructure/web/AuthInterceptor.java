package edu.udistrital.sig.infrastructure.web;

import edu.udistrital.sig.application.service.AuthService;
import edu.udistrital.sig.domain.exception.AuthenticationRequiredException;
import edu.udistrital.sig.domain.exception.UnauthorizedOperationException;
import edu.udistrital.sig.domain.model.Role;
import edu.udistrital.sig.domain.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new AuthenticationRequiredException();
        }
        String token = header.substring("Bearer ".length());
        User user = authService.resolve(token);

        if (isWrite(request) && user.role() != Role.ADMINISTRATOR) {
            throw new UnauthorizedOperationException();
        }
        request.setAttribute("currentUser", user);
        return true;
    }

    private boolean isWrite(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
    }
}
