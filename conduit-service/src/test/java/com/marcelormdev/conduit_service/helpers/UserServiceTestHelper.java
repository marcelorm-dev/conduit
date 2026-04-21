package com.marcelormdev.conduit_service.helpers;

import java.util.HashMap;
import java.util.Map;

import com.marcelormdev.conduit_service.article.ArticleService;
import com.marcelormdev.conduit_service.profile.Profile;
import com.marcelormdev.conduit_service.profile.ProfileRepository;
import com.marcelormdev.conduit_service.user.RegisterUserRequest;
import com.marcelormdev.conduit_service.user.UpdateUserRequest;
import com.marcelormdev.conduit_service.user.User;
import com.marcelormdev.conduit_service.user.UserRepository;
import com.marcelormdev.conduit_service.user.UserResponse;
import com.marcelormdev.conduit_service.user.UserService;

import tools.jackson.databind.ObjectMapper;

public class UserServiceTestHelper {

    private UserService userService;
    private UserRepository userRepository;
    private ProfileRepository profileRepository;
    private ArticleService articleService;

    private UserResponse userResponse;
    private User user;
    private Profile profile;

    UserServiceTestHelper(UserService userService, UserRepository userRepository,
            ProfileRepository profileRepository, ArticleService articleService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.articleService = articleService;
    }

    UserServiceTestHelper register(String username, String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("email", email);
        params.put("password", password);

        RegisterUserRequest request = createRequest(params, RegisterUserRequest.class);
        this.userResponse = userService.register(request);

        return this;
    }

    UserServiceTestHelper register(String username, String email, String password, String bio, String image) {
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("email", email);
        params.put("password", password);
        params.put("bio", bio);
        params.put("image", image);

        RegisterUserRequest request = createRequest(params, RegisterUserRequest.class);
        this.userResponse = userService.register(request);

        return this;
    }

    UserServiceTestHelper update(String token, Map<String, String> params) {
        UpdateUserRequest request = createRequest(params, UpdateUserRequest.class);
        this.userResponse = userService.update(token, request);

        return this;
    }

    private <T> T createRequest(Map<String, String> params, Class<T> requestClass) {
        Map<String, Map<String, String>> map = new HashMap<>();
        map.put("user", params);

        String json = new ObjectMapper().writeValueAsString(map);
        return new ObjectMapper().readValue(json, requestClass);
    }

    public UserResponse getUserResponse() {
        return userResponse;
    }

    public User getUser() {
        if (user == null) {
            user = userRepository.findByEmail(userResponse.user().email()).orElseThrow();
        }

        return user;
    }

    public Profile getProfile() {
        if (profile == null) {
            profile = profileRepository.findByUserEmail(userResponse.user().email()).orElseThrow();
        }
        return profile;
    }

    public String getToken() {
        return userResponse.user().token();
    }

    public ArticleServiceTestHelper createArticle(String title, String description, String body, String[] tags) {
        ArticleServiceTestHelper articleHelper = new ArticleServiceTestHelper(articleService);
        return articleHelper.createArticle(getToken(), title, description, body, tags);
    }

}
