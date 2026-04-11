package com.marcelormdev.conduit_service.auth;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.InvalidTokenException;
import com.marcelormdev.conduit_service.profile.Profile;
import com.marcelormdev.conduit_service.profile.ProfileRepository;
import com.marcelormdev.conduit_service.user.User;
import com.marcelormdev.conduit_service.user.UserRepository;

@Component
public class AuthService {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    public boolean isTokenValid(String token) {
        return jwtTokenService.isTokenValid(token);
    }

    public String generateToken(String token) {
        return jwtTokenService.generateToken(token);
    }

    public User authenticateUser(String token) {
        return authenticate(token, userRepository::findByEmail);
    }

    public Profile authenticateProfile(String token) {
        return authenticate(token, profileRepository::findByUserEmail);
    }

    private <T> T authenticate(String token, Function<String, Optional<T>> finder) {
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
