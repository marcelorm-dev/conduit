package com.marcelormdev.conduit_service.user;

public record UserResponse(Params user) {

    public UserResponse(User user) {
        this(new Params(user.getEmail(), user.getUsername(), user.getBio(), user.getImage(), user.getToken()));
    }

    public record Params(String email, String username, String bio, String image, String token) {

    }

}
