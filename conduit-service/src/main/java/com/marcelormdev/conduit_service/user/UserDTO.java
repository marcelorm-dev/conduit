package com.marcelormdev.conduit_service.user;

record UserDTO(String email, String password, String username, String bio, String image, String token) {
    UserDTO(User user) {
        this(user.getEmail(), user.getPassword(), user.getUsername(), user.getBio(), user.getImage(), user.getToken());
    }
}
