package com.marcelormdev.conduit_service.user;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
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

    public String getEmail() {
        return email;
    }

    void setEmail(String email) {
        if (email != null && !email.isBlank())
            this.email = email;
    }

    String getPassword() {
        return password;
    }

    void setPassword(String password) {
        if (password != null && !password.isBlank())
            this.password = password;
    }

    public String getUsername() {
        return username;
    }

    void setUsername(String username) {
        if (username != null && !username.isBlank())
            this.username = username;
    }

    public String getBio() {
        return bio;
    }

    void setBio(String bio) {
        this.bio = bio == null || bio.isBlank() ? null : bio;
    }

    public String getImage() {
        return image;
    }

    void setImage(String image) {
        this.image = image == null || image.isBlank() ? null : image;
    }

    String getToken() {
        return token;
    }

    void setToken(String token) {
        if (token != null && !token.isBlank())
            this.token = token;
    }

    void update(String username, String email, String password, String bio, String image, String token) {
        this.setUsername(username);
        this.setEmail(email);
        this.setPassword(password);
        this.setToken(token);
        this.setBio(bio);
        this.setImage(image);

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

        return Objects.equals(this.id, user.id);
    }

    @Override
    public String toString() {
        return String.format("Email: %s - Token: %s", email, token);
    }

}
