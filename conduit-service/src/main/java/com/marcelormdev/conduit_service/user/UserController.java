package com.marcelormdev.conduit_service.user;

import java.util.List;

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

  private record BodyRequest(UserRequest user) {

    private record UserRequest(String email, String username, String bio, String image, String token, String password) {
      UserDTO toDTO() {
        return new UserDTO(email, password, username, bio, image, token);
      }
    }

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
    UserDTO user = userService.currentUser(token);

    return BodyResponse.of(user);
  }

  @PostMapping("/api/users/login")
  public BodyResponse authenticate(@RequestBody BodyRequest body) {
    UserDTO dto = body.user.toDTO();
    UserDTO user = userService.login(dto.email(), dto.password());

    return BodyResponse.of(user);
  }

  @PostMapping("/api/users")
  @ResponseStatus(HttpStatus.CREATED)
  public BodyResponse register(@RequestBody BodyRequest body) {
    UserDTO user = userService.register(body.user.toDTO());

    return BodyResponse.of(user);
  }

  @PutMapping("/api/user")
  public BodyResponse update(@RequestHeader HttpHeaders headers, @RequestBody BodyRequest body) {
    String token = new JwtAuthorizationHeader(headers).getToken();
    UserDTO user = userService.update(token, body.user.toDTO());

    return BodyResponse.of(user);
  }

  @GetMapping("/api/users")
  public List<BodyResponse> getAllUsers(@RequestHeader HttpHeaders headers) {
    String token = new JwtAuthorizationHeader(headers).getToken();
    return userService.getAllUsers(token).stream().map(BodyResponse::of).toList();
  }

  @PutMapping("/api/token")
  public BodyResponse renewToken(@RequestBody BodyRequest body) {
    UserDTO user = userService.renewToken(body.user.email);

    return BodyResponse.of(user);
  }

}