package com.marcelormdev.conduit_service.profile;

import java.util.HashMap;
import java.util.Map;

public class ProfileDTO {

    static final String USERNAME = "username";
    static final String BIO = "bio";
    static final String IMAGE = "image";
    static final String FOLLOWING = "following";

    private final Map<String, Object> fields = new HashMap<>();

    ProfileDTO(Profile profile, boolean following) {
        this.fields.put(USERNAME, profile.getUsername());
        this.fields.put(BIO, profile.getBio());
        this.fields.put(IMAGE, profile.getImage());
        this.fields.put(FOLLOWING, following);
    }

    static ProfileDTO of(Profile profile, boolean following) {
        return new ProfileDTO(profile, following);
    }

    public String username() {
        return (String) this.fields.get(USERNAME);
    }

    public String bio() {
        return (String) this.fields.get(BIO);
    }

    public String image() {
        return (String) this.fields.get(IMAGE);
    }

    public boolean following() {
        return (boolean) this.fields.get(FOLLOWING);
    }

}
