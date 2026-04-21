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
import com.marcelormdev.conduit_service.helpers.TestHelper;
import com.marcelormdev.conduit_service.user.UserResponse;
import com.marcelormdev.conduit_service.user.UserRepository;

@SpringBootTest
class ProfileServiceTest {

    @Autowired
    private TestHelper helper;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void beforeEachTest() {        
        userRepository.deleteAll();
    }

    @Test
    void getProfile_returnsProfile_withoutToken() {
        helper.register("celeb", "celeb@test.com", "123456");

        ProfileResponse response = profileService.getProfile("celeb", null);

        assertEquals("celeb", response.profile().username());
        assertFalse(response.profile().following());
    }

    @Test
    void getProfile_returnsProfileWithFollowingFalse_whenAuthenticatedButNotFollowing() {
        String token = helper.register("prof", "prof@test.com", "123456").getToken();
        helper.register("celeb", "celeb@test.com", "654321");

        ProfileResponse response = profileService.getProfile("celeb", token);

        assertEquals("celeb", response.profile().username());
        assertFalse(response.profile().following());
    }

    @Test
    void getProfile_returnsProfileWithFollowingTrue_whenAuthenticatedAndFollowing() {
        String token = helper.register("prof", "prof@test.com", "123456").getToken();
        helper.register("celeb", "celeb@test.com", "654321");

        profileService.follow("celeb", token);
        ProfileResponse response = profileService.getProfile("celeb", token);

        assertEquals("celeb", response.profile().username());
        assertTrue(response.profile().following());
    }

    @Test
    void getProfile_returnsFollowingFalse_whenTokenIsInvalid() {
        helper.register("celeb", "celeb@test.com", "654321");

        ProfileResponse response = profileService.getProfile("celeb", "invalid-token");

        assertEquals("celeb", response.profile().username());
        assertFalse(response.profile().following());
    }

    @Test
    void getProfile_throwsAuthenticationException_whenProfileNotFoundForAuthenticatedUser() {
        UserResponse prof = helper.register("prof", "prof@test.com", "123456").getUserResponse();
        helper.register("celeb", "celeb@test.com", "654321");

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
        String token = helper.register("prof", "prof@test.com", "123456").getToken();
        helper.register("celeb", "celeb@test.com", "654321");

        ProfileResponse response = profileService.follow("celeb", token);

        assertEquals("celeb", response.profile().username());
        assertTrue(response.profile().following());
    }

    @Test
    void follow_persistsFollowRelationship() {
        String token = helper.register("prof", "prof@test.com", "123456").getToken();
        helper.register("celeb", "celeb@test.com", "654321");

        profileService.follow("celeb", token);
        ProfileResponse response = profileService.getProfile("celeb", token);

        assertTrue(response.profile().following());
    }

    @Test
    void follow_throwsException_whenTokenIsNullOrBlank() {
        helper.register("celeb", "celeb@test.com", "654321");

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
        helper.register("celeb", "celeb@test.com", "654321");

        String invalidToken = "aaaaaaa";
        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.follow("celeb", invalidToken));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED,
                exception.getMessagesAsString());
    }

    @Test
    void follow_throwsException_whenUsernameNotFound() {
        String token = helper.register("prof", "prof@test.com", "123456").getToken();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> profileService.follow("unknown", token));
        assertEquals(ErrorMessages.USERNAME_NOT_FOUND,
                exception.getMessagesAsString());
    }

    @Test
    void follow_throwsAuthenticationException_whenProfileNotFoundForAuthenticatedUser() {
        UserResponse prof = helper.register("prof", "prof@test.com", "123456").getUserResponse();
        helper.register("celeb", "celeb@test.com", "654321");

        profileRepository.findByUserEmail(prof.user().email()).ifPresent(profileRepository::delete);

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.follow("celeb", prof.user().token()));
        assertEquals(ErrorMessages.ACCESS_DENIED_EMAIL_NOT_FOUND,
                exception.getMessagesAsString());
    }

    @Test
    void unfollow_returnsProfileWithFollowingFalse_whenDatasAreValid() {
        String token = helper.register("prof", "prof@test.com", "123456").getToken();
        helper.register("celeb", "celeb@test.com", "654321");

        profileService.follow("celeb", token);
        profileService.unfollow("celeb", token);
        ProfileResponse response = profileService.getProfile("celeb", token);

        assertFalse(response.profile().following());
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
        String token = helper.register("prof", "prof@test.com", "123456").getToken();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> profileService.unfollow("unknown", token));
        assertEquals(ErrorMessages.USERNAME_NOT_FOUND,
                exception.getMessagesAsString());
    }

    @Test
    void unfollow_throwsAuthenticationException_whenProfileNotFoundForAuthenticatedUser() {
        UserResponse prof = helper.register("prof", "prof@test.com", "123456").getUserResponse();
        helper.register("celeb", "celeb@test.com", "654321");

        profileRepository.findByUserEmail(prof.user().email()).ifPresent(profileRepository::delete);

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.unfollow("celeb", prof.user().token()));
        assertEquals(ErrorMessages.ACCESS_DENIED_EMAIL_NOT_FOUND,
                exception.getMessagesAsString());
    }

}
