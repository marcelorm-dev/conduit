package com.marcelormdev.conduit_service.user;

public record RegisterUserRequest(Params user) {

    public RegisterUserRequest(String email, String password, String username, String bio, String image) {
        this(new Params(email, password, username, bio, image));
    }

    public record Params(String email, String password, String username, String bio, String image) {

    }

}
