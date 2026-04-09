package com.marcelormdev.conduit_service.user;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.marcelormdev.conduit_service.common.http.AuthorizationHeader;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/user")
    public UserResponse currentUser(@RequestHeader HttpHeaders headers) {
        String token = new AuthorizationHeader(headers).getToken();
        return userService.currentUser(token);
    }

    @PostMapping("/api/users/login")
    public UserResponse authenticate(@RequestBody LoginUserRequest request) {
        return userService.login(request);
    }

    @PostMapping("/api/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody RegisterUserRequest request) {
        return userService.register(request);
    }

    @PutMapping("/api/user")
    public UserResponse update(@RequestHeader HttpHeaders headers, @RequestBody UpdateUserRequest request) {
        String token = new AuthorizationHeader(headers).getToken();
        return userService.update(token, request);
    }

    @GetMapping("/api/users")
    public List<UserResponse> getAllUsers(@RequestHeader HttpHeaders headers) {
        String token = new AuthorizationHeader(headers).getToken();
        return userService.getAllUsers(token);
    }

    @PutMapping("/api/token")
    public UserResponse renewToken(@RequestBody Map<String, Map<String, String>> body) {
        return userService.renewToken(body.get("user").get("email"));
    }

}
