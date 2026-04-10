package com.marcelormdev.conduit_service.auth;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.InvalidTokenException;

@Component
public class AuthService {

    private final JwtTokenService jwtTokenService;

    public AuthService(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public boolean isTokenValid(String token) {
        return jwtTokenService.isTokenValid(token);
    }

    public String generateToken(String token) {
        return jwtTokenService.generateToken(token);
    }

    public <T> T authenticate(String token, Function<String, Optional<T>> finder) {
        String email;
        try {
            email = jwtTokenService.extractEmail(token);
        } catch (InvalidTokenException e) {
            throw new AuthenticationException(e.getMessage());
        }

        return finder.apply(email)
                .orElseThrow(() -> new AuthenticationException(ErrorMessages.ACCESS_DENIED_EMAIL_NOT_FOUND));
    }

}
