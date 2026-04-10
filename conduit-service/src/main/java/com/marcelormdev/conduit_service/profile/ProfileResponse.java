package com.marcelormdev.conduit_service.profile;

public record ProfileResponse(Params profile) {

    public ProfileResponse(Profile profile, boolean following) {
        this(new Params(profile.getUsername(), profile.getBio(), profile.getImage(), following));
    }

    public record Params(String username, String bio, String image, boolean following) {

    }
}
