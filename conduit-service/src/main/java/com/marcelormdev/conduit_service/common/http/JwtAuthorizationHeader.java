package com.marcelormdev.conduit_service.common.http;

import org.springframework.http.HttpHeaders;

public class JwtAuthorizationHeader {

    private final String jwtToken;

    public JwtAuthorizationHeader(HttpHeaders headers) {
        this.jwtToken = extractJwtToken(headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    private String extractJwtToken(String authorization) {
        if (authorization == null)
            return null;

        return authorization.startsWith("Token ") ? authorization.replace("Token ", "") : null;
    }

    public String getToken() {
        return this.jwtToken;
    }

}
