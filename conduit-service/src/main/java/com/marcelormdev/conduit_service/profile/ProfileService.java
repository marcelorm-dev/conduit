package com.marcelormdev.conduit_service.profile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.common.security.JwtTokenService;
import com.marcelormdev.conduit_service.user.UserDTO;
import com.marcelormdev.conduit_service.user.UserService;
import com.marcelormdev.conduit_service.user.UserService.UserRegisteredEvent;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    ProfileService(ProfileRepository profileRepository, UserService userService, JwtTokenService jwtTokenService) {
        this.profileRepository = profileRepository;
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    private Profile getAuthenticatedProfile(String token) {
        UserDTO userDTO = userService.currentUser(token);
        return profileRepository.findByUserEmail(userDTO.email())
                .orElseThrow(() -> new AuthenticationException(ErrorMessages.EMAIL_NOT_FOUND));
    }

    private Profile findByUsername(String username) {
        return profileRepository.findByUserUsername(username)
                .orElseThrow(() -> new FieldValidationException(ErrorMessages.USERNAME_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ProfileDTO getProfile(String username, String token) {
        Profile targetProfile = findByUsername(username);

        boolean following = token != null && jwtTokenService.isTokenValid(token)
                ? getAuthenticatedProfile(token).isFollowing(targetProfile)
                : false;

        return ProfileDTO.of(targetProfile, following);
    }

    @Transactional
    public ProfileDTO follow(String username, String token) {
        Profile currentUserProfile = getAuthenticatedProfile(token);
        Profile targetProfile = findByUsername(username);

        currentUserProfile.follow(targetProfile);
        profileRepository.save(currentUserProfile);

        boolean following = currentUserProfile.isFollowing(targetProfile);

        return ProfileDTO.of(targetProfile, following);
    }

    @Transactional
    public ProfileDTO unfollow(String username, String token) {
        Profile currentUserProfile = getAuthenticatedProfile(token);
        Profile targetProfile = findByUsername(username);

        currentUserProfile.unfollow(targetProfile);
        profileRepository.save(currentUserProfile);

        boolean following = currentUserProfile.isFollowing(targetProfile);

        return ProfileDTO.of(targetProfile, following);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        Profile profile = new Profile(event.user());
        profileRepository.save(profile);
    }

}
