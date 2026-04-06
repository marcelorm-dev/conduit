package com.marcelormdev.conduit_service.apitest;

import org.springframework.test.web.servlet.client.RestTestClient;

public class UserRestCaller {

    private final RestCaller restCaller;

    public UserRestCaller(RestTestClient restClient) {
        this.restCaller = new RestCaller(restClient);
    }

    public RestTestClient.ResponseSpec callRegisterAPI(String body) {
        return restCaller.post("/api/users", null, body);
    }

    public RestTestClient.ResponseSpec callLoginAPI(String body) {
        return restCaller.post("/api/users/login", null, body);
    }

    public RestTestClient.ResponseSpec callListUsersAPI(String token) {
        return restCaller.get("/api/users", token);
    }

    public RestTestClient.ResponseSpec callCurrentUserAPI(String token) {
        return restCaller.get("/api/user", token);
    }

    public RestTestClient.ResponseSpec callUpdateUserAPI(String token, String body) {
        return restCaller.put("/api/user", token, body);
    }

    public RestTestClient.ResponseSpec callRenewTokenAPI(String body) {
        return restCaller.put("/api/token", null, body);
    }

}
