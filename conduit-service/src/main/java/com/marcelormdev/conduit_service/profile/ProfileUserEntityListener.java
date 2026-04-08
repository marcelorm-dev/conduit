package com.marcelormdev.conduit_service.profile;

import org.springframework.stereotype.Component;

import com.marcelormdev.conduit_service.user.User;

import jakarta.persistence.PostPersist;

@Component
public class ProfileUserEntityListener {

    ProfileRepository profileRepository;

    ProfileUserEntityListener(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @PostPersist
    public void postSave(Object obj) {
        if (obj instanceof User user) {
            Profile profile = new Profile(user);
            profileRepository.save(profile);
        }
    }

}
