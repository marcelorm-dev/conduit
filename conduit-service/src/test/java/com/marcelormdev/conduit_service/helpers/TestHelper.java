package com.marcelormdev.conduit_service.helpers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.marcelormdev.conduit_service.article.ArticleService;
import com.marcelormdev.conduit_service.auth.AuthService;
import com.marcelormdev.conduit_service.profile.ProfileRepository;
import com.marcelormdev.conduit_service.user.UserRepository;
import com.marcelormdev.conduit_service.user.UserService;

@Component
public class TestHelper {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ArticleService articleService;

    public String generateToken(String email) {
        return authService.generateToken(email);
    }

    public UserServiceTestHelper register(String username, String email, String password) {
        UserServiceTestHelper service = new UserServiceTestHelper(userService, userRepository,
                profileRepository, articleService);
        return service.register(username, email, password);
    }

    public UserServiceTestHelper register(String username, String email, String password, String bio, String image) {
        UserServiceTestHelper service = new UserServiceTestHelper(userService, userRepository,
                profileRepository, articleService);
        return service.register(username, email, password, bio, image);
    }

    public UserServiceTestHelper update(String token, Map<String, String> params) {
        UserServiceTestHelper service = new UserServiceTestHelper(userService, userRepository,
                profileRepository, articleService);
        return service.update(token, params);
    }

}
