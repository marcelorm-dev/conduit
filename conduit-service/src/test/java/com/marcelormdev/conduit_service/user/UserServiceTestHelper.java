package com.marcelormdev.conduit_service.user;

import java.util.HashMap;
import java.util.Map;

import tools.jackson.databind.ObjectMapper;

public class UserServiceTestHelper {

    private UserService userService;

    public UserServiceTestHelper(UserService userService) {
        this.userService = userService;
    }

    private <T> T createRequest(Map<String, String> params, Class<T> requestClass) {
        Map<String, Map<String, String>> map = new HashMap<>();
        map.put("user", params);

        String json = new ObjectMapper().writeValueAsString(map);
        return new ObjectMapper().readValue(json, requestClass);
    }

    public UserResponse registerUser(String username, String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("email", email);
        params.put("password", password);

        RegisterUserRequest request = createRequest(params, RegisterUserRequest.class);
        return userService.register(request);
    }

    public UserResponse registerUser(String username, String email, String password, String bio, String image) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("email", email);
        params.put("password", password);
        params.put("bio", bio);
        params.put("image", image);

        RegisterUserRequest request = createRequest(params, RegisterUserRequest.class);
        return userService.register(request);
    }

    public UserResponse updateUser(String token, Map<String, String> params) {
        UpdateUserRequest request = createRequest(params, UpdateUserRequest.class);
        return userService.update(token, request);
    }

}
