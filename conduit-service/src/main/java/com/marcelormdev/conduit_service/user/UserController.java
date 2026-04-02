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

import com.marcelormdev.conduit_service.http.JwtAuthorizationHeader;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    private record BodyResponse(UserResponse user) {

        private record UserResponse(String email, String username, String bio, String image, String token) {
        }

        static BodyResponse of(UserDTO userDTO) {
            UserResponse userResponse = new UserResponse(
                    userDTO.email(),
                    userDTO.username(),
                    userDTO.bio(),
                    userDTO.image(),
                    userDTO.token());

            return new BodyResponse(userResponse);
        }

    }

    @GetMapping("/api/user")
    public BodyResponse currentUser(@RequestHeader HttpHeaders headers) {
        String token = new JwtAuthorizationHeader(headers).getToken();
        UserDTO currentUser = userService.currentUser(token);

        return BodyResponse.of(currentUser);
    }

    @PostMapping("/api/users/login")
    public BodyResponse authenticate(@RequestBody Map<String, Map<String, String>> body) {
        UserDTO requestUser = new UserDTO(body.get("user"));
        UserDTO loggedUser = userService.login(requestUser.email(), requestUser.password());

        return BodyResponse.of(loggedUser);
    }

    @PostMapping("/api/users")
    @ResponseStatus(HttpStatus.CREATED)
    public BodyResponse register(@RequestBody Map<String, Map<String, String>> body) {
        UserDTO registeredUser = userService.register(new UserDTO(body.get("user")));

        return BodyResponse.of(registeredUser);
    }

    @PutMapping("/api/user")
    public BodyResponse update(@RequestHeader HttpHeaders headers, @RequestBody Map<String, Map<String, String>> body) {
        String token = new JwtAuthorizationHeader(headers).getToken();
        UserDTO updatedUser = userService.update(token, new UserDTO(body.get("user")));

        return BodyResponse.of(updatedUser);
    }

    @GetMapping("/api/users")
    public List<BodyResponse> getAllUsers(@RequestHeader HttpHeaders headers) {
        String token = new JwtAuthorizationHeader(headers).getToken();
        return userService.getAllUsers(token).stream().map(BodyResponse::of).toList();
    }

    @PutMapping("/api/token")
    public BodyResponse renewToken(@RequestBody Map<String, Map<String, String>> body) {
        UserDTO user = userService.renewToken(body.get("user").get("email"));

        return BodyResponse.of(user);
    }

}
