package com.marcelormdev.conduit_service.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.user.RegisterUserRequest;
import com.marcelormdev.conduit_service.user.UserResponse;
import com.marcelormdev.conduit_service.user.UserRepository;
import com.marcelormdev.conduit_service.user.UserService;
import com.marcelormdev.conduit_service.user.UserServiceTestHelper;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    private UserServiceTestHelper userServiceHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void beforeEachTest() {
        userServiceHelper = new UserServiceTestHelper(userService);
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private UserResponse registerUser(String username, String email, String password) {
        return userServiceHelper.registerUser(username, email, password);
    }

    @Test
    void getProfile_returnsProfile_withoutToken() {
        registerUser("celeb", "celeb@test.com", "123456");

        ProfileDTO profile = profileService.getProfile("celeb", null);

        assertEquals("celeb", profile.username());
        assertFalse(profile.following());
    }

    @Test
    void getProfile_returnsProfileWithFollowingFalse_whenAuthenticatedButNotFollowing() {
        String token = registerUser("prof", "prof@test.com", "123456").user().token();
        registerUser("celeb", "celeb@test.com", "654321");

        ProfileDTO profile = profileService.getProfile("celeb", token);

        assertEquals("celeb", profile.username());
        assertFalse(profile.following());
    }

    @Test
    void getProfile_returnsProfileWithFollowingTrue_whenAuthenticatedAndFollowing() {
        String token = registerUser("prof", "prof@test.com", "123456").user().token();
        registerUser("celeb", "celeb@test.com", "654321");

        profileService.follow("celeb", token);
        ProfileDTO profile = profileService.getProfile("celeb", token);

        assertEquals("celeb", profile.username());
        assertTrue(profile.following());
    }

    @Test
    void getProfile_returnsFollowingFalse_whenTokenIsInvalid() {
        registerUser("celeb", "celeb@test.com", "654321");

        ProfileDTO profile = profileService.getProfile("celeb", "invalid-token");

        assertEquals("celeb", profile.username());
        assertFalse(profile.following());
    }

    @Test
    void getProfile_throwsAuthenticationException_whenProfileNotFoundForAuthenticatedUser() {
        UserResponse prof = registerUser("prof", "prof@test.com", "123456");
        registerUser("celeb", "celeb@test.com", "654321");

        profileRepository.findByUserEmail(prof.user().email()).ifPresent(profileRepository::delete);

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.getProfile("celeb", prof.user().token()));
        assertEquals(ErrorMessages.ACCESS_DENIED_EMAIL_NOT_FOUND,
                exception.getMessagesAsString());
    }

    @Test
    void getProfile_throwsException_whenUsernameNotFound() {
        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> profileService.getProfile("unknown", null));
        assertEquals(ErrorMessages.USERNAME_NOT_FOUND,
                exception.getMessagesAsString());
    }

    @Test
    void follow_returnsProfileWithFollowingTrue_whenDatasAreValid() {
        String token = registerUser("prof", "prof@test.com", "123456").user().token();
        registerUser("celeb", "celeb@test.com", "654321");

        ProfileDTO profile = profileService.follow("celeb", token);

        assertEquals("celeb", profile.username());
        assertTrue(profile.following());
    }

    @Test
    void follow_persistsFollowRelationship() {
        String token = registerUser("prof", "prof@test.com", "123456").user().token();
        registerUser("celeb", "celeb@test.com", "654321");

        profileService.follow("celeb", token);
        ProfileDTO profile = profileService.getProfile("celeb", token);

        assertTrue(profile.following());
    }

    @Test
    void follow_throwsException_whenTokenIsNullOrBlank() {
        registerUser("celeb", "celeb@test.com", "654321");

        String[] nullOrBlankTokens = new String[] { null, " " };
        for (String token : nullOrBlankTokens) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> profileService.follow("celeb", token));
            assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void follow_throwsException_whenTokenIsInvalid() {
        registerUser("celeb", "celeb@test.com", "654321");

        String invalidToken = "aaaaaaa";
        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.follow("celeb", invalidToken));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED,
                exception.getMessagesAsString());
    }

    @Test
    void follow_throwsException_whenUsernameNotFound() {
        String token = registerUser("prof", "prof@test.com", "123456").user().token();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> profileService.follow("unknown", token));
        assertEquals(ErrorMessages.USERNAME_NOT_FOUND,
                exception.getMessagesAsString());
    }

    @Test
    void follow_throwsAuthenticationException_whenProfileNotFoundForAuthenticatedUser() {
        UserResponse prof = registerUser("prof", "prof@test.com", "123456");
        registerUser("celeb", "celeb@test.com", "654321");

        profileRepository.findByUserEmail(prof.user().email()).ifPresent(profileRepository::delete);

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.follow("celeb", prof.user().token()));
        assertEquals(ErrorMessages.ACCESS_DENIED_EMAIL_NOT_FOUND,
                exception.getMessagesAsString());
    }

    @Test
    void unfollow_returnsProfileWithFollowingFalse_whenDatasAreValid() {
        String token = registerUser("prof", "prof@test.com", "123456").user().token();
        registerUser("celeb", "celeb@test.com", "654321");

        profileService.follow("celeb", token);
        profileService.unfollow("celeb", token);
        ProfileDTO profile = profileService.getProfile("celeb", token);

        assertFalse(profile.following());
    }

    @Test
    void unfollow_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> profileService.unfollow("celeb", nullOrBlank));
            assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void unfollow_throwsException_whenTokenIsInvalid() {
        String invalidToken = "aaaaaaa";

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.unfollow("celeb", invalidToken));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED,
                exception.getMessagesAsString());
    }

    @Test
    void unfollow_throwsException_whenUsernameNotFound() {
        String token = registerUser("prof", "prof@test.com", "123456").user().token();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> profileService.unfollow("unknown", token));
        assertEquals(ErrorMessages.USERNAME_NOT_FOUND,
                exception.getMessagesAsString());
    }

    @Test
    void unfollow_throwsAuthenticationException_whenProfileNotFoundForAuthenticatedUser() {
        UserResponse prof = registerUser("prof", "prof@test.com", "123456");
        registerUser("celeb", "celeb@test.com", "654321");

        profileRepository.findByUserEmail(prof.user().email()).ifPresent(profileRepository::delete);

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.unfollow("celeb", prof.user().token()));
        assertEquals(ErrorMessages.ACCESS_DENIED_EMAIL_NOT_FOUND,
                exception.getMessagesAsString());
    }

}
