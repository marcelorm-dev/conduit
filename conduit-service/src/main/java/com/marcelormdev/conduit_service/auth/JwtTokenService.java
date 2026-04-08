package com.marcelormdev.conduit_service.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.InvalidTokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
class JwtTokenService {

    @Value("${jwt.secret-key}")
    private String secretKeyString;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey secretKey;

    @PostConstruct
    private void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    String extractEmail(String token) throws InvalidTokenException {
        if (token == null || token.isBlank())
            throw new InvalidTokenException(ErrorMessages.TOKEN_NOT_INFORMED);

        if (!isTokenValid(token))
            throw new InvalidTokenException(ErrorMessages.TOKEN_INVALID_OR_EXPIRED);

        return parseClaims(token).getSubject();
    }

    boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
