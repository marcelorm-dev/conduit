package com.marcelormdev.conduit_service.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.marcelormdev.conduit_service.auth.AuthService;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.user.UserRegisteredEvent;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AuthService authService;

    private Profile findByUsername(String username) {
        return profileRepository.findByUserUsername(username)
                .orElseThrow(() -> new FieldValidationException(ErrorMessages.USERNAME_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String username, String token) {
        Profile targetProfile = findByUsername(username);

        boolean following = false;
        if (token != null && authService.isTokenValid(token)) {
            Profile currentUserProfile = authService.authenticateProfile(token);
            following = currentUserProfile.isFollowing(targetProfile);
        }

        return new ProfileResponse(targetProfile, following);
    }

    @Transactional
    public ProfileResponse follow(String username, String token) {
        Profile currentUserProfile = authService.authenticateProfile(token);
        Profile targetProfile = findByUsername(username);

        currentUserProfile.follow(targetProfile);
        profileRepository.save(currentUserProfile);

        boolean following = currentUserProfile.isFollowing(targetProfile);

        return new ProfileResponse(targetProfile, following);
    }

    @Transactional
    public ProfileResponse unfollow(String username, String token) {
        Profile currentUserProfile = authService.authenticateProfile(token);
        Profile targetProfile = findByUsername(username);

        currentUserProfile.unfollow(targetProfile);
        profileRepository.save(currentUserProfile);

        boolean following = currentUserProfile.isFollowing(targetProfile);

        return new ProfileResponse(targetProfile, following);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        Profile profile = new Profile(event.user());
        profileRepository.save(profile);
    }

}
