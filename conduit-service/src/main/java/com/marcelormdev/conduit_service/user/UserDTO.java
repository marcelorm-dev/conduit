package com.marcelormdev.conduit_service.user;

import java.util.HashMap;
import java.util.Map;

class UserDTO {

    static final String EMAIL = "email";
    static final String PASSWORD = "password";
    static final String USERNAME = "username";
    static final String BIO = "bio";
    static final String IMAGE = "image";
    static final String TOKEN = "token";

    private Map<String, String> fields = new HashMap<>();

    UserDTO(Map<String, String> fields) {
        this.fields = fields;
    }

    UserDTO(User user) {
        this.fields.put(EMAIL, user.getEmail());
        this.fields.put(PASSWORD, user.getPassword());
        this.fields.put(USERNAME, user.getUsername());
        this.fields.put(TOKEN, user.getToken());
        this.fields.put(BIO, user.getBio());
        this.fields.put(IMAGE, user.getImage());
    }

    String email() {
        return this.fields.get(EMAIL);
    }

    String password() {
        return this.fields.get(PASSWORD);
    }

    String username() {
        return this.fields.get(USERNAME);
    }

    String bio() {
        return this.fields.get(BIO);
    }

    boolean hasBio() {
        return this.fields.containsKey(BIO);
    }

    String image() {
        return this.fields.get(IMAGE);
    }

    boolean hasImage() {
        return this.fields.containsKey(IMAGE);
    }

    String token() {
        return this.fields.get(TOKEN);
    }

}
