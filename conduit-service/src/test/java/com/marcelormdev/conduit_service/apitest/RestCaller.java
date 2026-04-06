package com.marcelormdev.conduit_service.apitest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient.RequestBodySpec;
import org.springframework.test.web.servlet.client.RestTestClient.RequestHeadersSpec;

class RestCaller {

    private RestTestClient client;

    RestCaller(RestTestClient client) {
        this.client = client;
    }

    RestTestClient.ResponseSpec post(String uri, String jwtToken, String body) {
        RequestBodySpec request = client.post().uri(uri);

        if (jwtToken != null && !jwtToken.isBlank())
            request.header("Authorization", "Token " + jwtToken);

        if (body != null && !body.isBlank())
            request.contentType(MediaType.APPLICATION_JSON).body(body);

        return request.exchange();
    }

    RestTestClient.ResponseSpec get(String uri, String jwtToken) {
        RequestHeadersSpec request = client.get().uri(uri);

        if (jwtToken != null && !jwtToken.isBlank())
            request.header("Authorization", "Token " + jwtToken);

        return request.exchange();
    }

    RestTestClient.ResponseSpec put(String uri, String jwtToken, String body) {
        RequestBodySpec request = client.put().uri(uri);

        if (jwtToken != null && !jwtToken.isBlank())
            request.header("Authorization", "Token " + jwtToken);

        if (body != null && !body.isBlank())
            request.contentType(MediaType.APPLICATION_JSON).body(body);

        return request.exchange();
    }

    RestTestClient.ResponseSpec delete(String uri, String jwtToken) {
        return client.delete().uri(uri)
                .header("Authorization", "Token " + jwtToken)
                .exchange();
    }

}
