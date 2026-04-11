package com.marcelormdev.conduit_service.profile;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.marcelormdev.conduit_service.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "profiles")
public class Profile {

    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "profile_follows", joinColumns = @JoinColumn(name = "follower_id"), inverseJoinColumns = @JoinColumn(name = "followed_id"))
    private Set<Profile> following = new HashSet<>();

    Profile() {
    }

    public Profile(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getBio() {
        return user.getBio();
    }

    public String getImage() {
        return user.getImage();
    }

    String getEmail() {
        return user.getEmail();
    }

    public void follow(Profile profile) {
        this.following.add(profile);
    }

    public void unfollow(Profile profile) {
        this.following.remove(profile);
    }

    public boolean isFollowing(Profile profile) {
        return this.following.contains(profile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Profile profile))
            return false;
        return Objects.equals(this.id, profile.id);
    }

}
