package com.marcelormdev.conduit_service.user;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
class User {

    private @Id @GeneratedValue Long id;
    private String username;
    private String email;
    private String password;
    private String token;
    private String bio;
    private String image;

    User() {

    }

    User(String email, String password, String username, String bio, String image, String token) {
        this.setEmail(email);
        this.setPassword(password);
        this.setUsername(username);
        this.setBio(bio);
        this.setImage(image);
        this.setToken(token);
    }

    String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

    String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    String getBio() {
        return bio;
    }

    void setBio(String bio) {
        this.bio = bio;
    }

    String getImage() {
        return image;
    }

    void setImage(String image) {
        this.image = image;
    }

    String getToken() {
        return token;
    }

    void setToken(String token) {
        this.token = token;
    }

    void update(String username, String email, String password, String bio, String image, String token) {
        if (username != null)
            this.setUsername(username);

        if (email != null)
            this.setEmail(email);

        if (password != null)
            this.setPassword(password);

        if (bio != null)
            this.setBio(bio);

        if (image != null)
            this.setImage(image);

        if (token != null)
            this.setToken(token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.username, this.email, this.password, this.token, this.bio, this.image);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof User user))
            return false;

        return Objects.equals(this.id, user.id) &&
                Objects.equals(this.username, user.username) &&
                Objects.equals(this.email, user.email) &&
                Objects.equals(this.password, user.password) &&
                Objects.equals(this.token, user.token) &&
                Objects.equals(this.bio, user.bio) &&
                Objects.equals(this.image, user.image);
    }

    @Override
    public String toString() {
        return String.format("Email: %s - Token: %s", email, token);
    }

}
