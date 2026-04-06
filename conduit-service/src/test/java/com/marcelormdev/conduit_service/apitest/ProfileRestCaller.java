package com.marcelormdev.conduit_service.apitest;

import org.springframework.test.web.servlet.client.RestTestClient;

public class ProfileRestCaller {

    private final RestCaller restCaller;

    public ProfileRestCaller(RestTestClient restClient) {
        this.restCaller = new RestCaller(restClient);
    }

    public RestTestClient.ResponseSpec callGetProfileAPI(String username) {
        return callGetProfileAPI(username, null);
    }

    public RestTestClient.ResponseSpec callGetProfileAPI(String username, String token) {
        return restCaller.get("/api/profiles/" + username, token);
    }

    public RestTestClient.ResponseSpec callFollowAPI(String username, String token) {
        return restCaller.post("/api/profiles/" + username + "/follow", token, null);
    }

    public RestTestClient.ResponseSpec callUnfollowAPI(String username, String token) {
        return restCaller.delete("/api/profiles/" + username + "/follow", token);
    }

}
