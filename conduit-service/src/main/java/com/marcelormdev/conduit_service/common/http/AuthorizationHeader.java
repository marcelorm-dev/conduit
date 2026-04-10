package com.marcelormdev.conduit_service.common.http;

import org.springframework.http.HttpHeaders;

public class AuthorizationHeader {

    private String token;

    public AuthorizationHeader(HttpHeaders headers) {
        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null) {
            this.token = null;
        } else {
            if (authorization.startsWith("Token "))
                this.token = authorization.replace("Token ", "");
        }
    }

    public String getToken() {
        return this.token;
    }

}
