package com.marcelormdev.conduit_service.apitest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

class RestCaller {

    private RestTestClient client;

    RestCaller(RestTestClient client) {
        this.client = client;
    }

    public RestTestClient.ResponseSpec post(String uri, String body) {
        return client.post().uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    public RestTestClient.ResponseSpec get(String uri, String jwtToken) {
        return client.get().uri(uri)
                .header("Authorization", "Token " + jwtToken)
                .exchange();
    }

    public RestTestClient.ResponseSpec put(String uri, String jwtToken, String body) {
        return client.put().uri(uri)
                .header("Authorization", "Token " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    public RestTestClient.ResponseSpec put(String uri, String body) {
        return client.put().uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

}
