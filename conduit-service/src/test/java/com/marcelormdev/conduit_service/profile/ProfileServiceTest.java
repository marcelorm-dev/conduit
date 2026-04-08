package com.marcelormdev.conduit_service.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.commons.JsonToMapConverter;
import com.marcelormdev.conduit_service.user.UserDTO;
import com.marcelormdev.conduit_service.user.UserRepository;
import com.marcelormdev.conduit_service.user.UserService;

@SpringBootTest
class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void beforeEachTest() {
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private UserDTO jsonToUserDTO(String json) {
        JsonToMapConverter<Map<String, Map<String, String>>> jsonConverter = new JsonToMapConverter<>();
        Map<String, Map<String, String>> jsonAsMap = jsonConverter.convert(json);
        return new UserDTO(jsonAsMap.get("user"));
    }

    private UserDTO registerUser(String username, String email) {
        return userService.register(jsonToUserDTO("""
                {
                    "user": {
                        "username": "%s",
                        "email": "%s",
                        "password": "password123"
                    }
                }
                """.formatted(username, email)));
    }

    @Test
    void getProfile_returnsProfile_withoutToken() {
        registerUser("celeb", "celeb@test.com");

        ProfileDTO profile = profileService.getProfile("celeb", null);

        assertEquals("celeb", profile.username());
        assertFalse(profile.following());
    }

    @Test
    void getProfile_returnsProfileWithFollowingFalse_whenAuthenticatedButNotFollowing() {
        UserDTO prof = registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        ProfileDTO profile = profileService.getProfile("celeb", prof.token());

        assertEquals("celeb", profile.username());
        assertFalse(profile.following());
    }

    @Test
    void getProfile_returnsProfileWithFollowingTrue_whenAuthenticatedAndFollowing() {
        UserDTO prof = registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        profileService.follow("celeb", prof.token());
        ProfileDTO profile = profileService.getProfile("celeb", prof.token());

        assertEquals("celeb", profile.username());
        assertTrue(profile.following());
    }

    @Test
    void getProfile_returnsFollowingFalse_whenTokenIsInvalid() {
        registerUser("celeb", "celeb@test.com");

        ProfileDTO profile = profileService.getProfile("celeb", "invalid-token");

        assertEquals("celeb", profile.username());
        assertFalse(profile.following());
    }

    @Test
    void getProfile_throwsAuthenticationException_whenProfileNotFoundForAuthenticatedUser() {
        UserDTO prof = registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        profileRepository.findByUserEmail(prof.email()).ifPresent(profileRepository::delete);

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.getProfile("celeb", prof.token()));
        assertEquals(ErrorMessages.EMAIL_NOT_FOUND, exception.getMessagesAsString());
    }

    @Test
    void getProfile_throwsException_whenUsernameNotFound() {
        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> profileService.getProfile("unknown", null));
        assertEquals(ErrorMessages.USERNAME_NOT_FOUND, exception.getMessagesAsString());
    }

    @Test
    void follow_returnsProfileWithFollowingTrue_whenDatasAreValid() {
        UserDTO prof = registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        ProfileDTO profile = profileService.follow("celeb", prof.token());

        assertEquals("celeb", profile.username());
        assertTrue(profile.following());
    }

    @Test
    void follow_persistsFollowRelationship() {
        UserDTO prof = registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        profileService.follow("celeb", prof.token());
        ProfileDTO profile = profileService.getProfile("celeb", prof.token());

        assertTrue(profile.following());
    }

    @Test
    void follow_throwsException_whenTokenIsNullOrBlank() {
        registerUser("celeb", "celeb@test.com");

        String[] nullOrBlankTokens = new String[] { null, " " };
        for (String token : nullOrBlankTokens) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> profileService.follow("celeb", token));
            assertEquals(ErrorMessages.TOKEN_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void follow_throwsException_whenTokenIsInvalid() {
        UserDTO prof = registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        String invalidToken = prof.token() + "aaaaaaa";
        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.follow("celeb", invalidToken));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void follow_throwsException_whenUsernameNotFound() {
        UserDTO prof = registerUser("prof", "prof@test.com");

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> profileService.follow("unknown", prof.token()));
        assertEquals(ErrorMessages.USERNAME_NOT_FOUND, exception.getMessagesAsString());
    }

    @Test
    void follow_throwsAuthenticationException_whenProfileNotFoundForAuthenticatedUser() {
        UserDTO prof = registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        profileRepository.findByUserEmail(prof.email()).ifPresent(profileRepository::delete);

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.follow("celeb", prof.token()));
        assertEquals(ErrorMessages.EMAIL_NOT_FOUND, exception.getMessagesAsString());
    }

    @Test
    void unfollow_returnsProfileWithFollowingFalse_whenDatasAreValid() {
        UserDTO prof = registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        profileService.follow("celeb", prof.token());
        ProfileDTO profile = profileService.unfollow("celeb", prof.token());

        assertEquals("celeb", profile.username());
        assertFalse(profile.following());
    }

    @Test
    void unfollow_persistsUnfollowRelationship() {
        UserDTO prof = registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        profileService.follow("celeb", prof.token());
        profileService.unfollow("celeb", prof.token());
        ProfileDTO profile = profileService.getProfile("celeb", prof.token());

        assertFalse(profile.following());
    }

    @Test
    void unfollow_throwsException_whenTokenIsNullOrBlank() {
        registerUser("celeb", "celeb@test.com");

        String[] nullOrBlankTokens = new String[] { null, " " };
        for (String token : nullOrBlankTokens) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> profileService.unfollow("celeb", token));
            assertEquals(ErrorMessages.TOKEN_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void unfollow_throwsException_whenTokenIsInvalid() {
        UserDTO prof = registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        String invalidToken = prof.token() + "aaaaaaa";
        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.unfollow("celeb", invalidToken));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void unfollow_throwsException_whenUsernameNotFound() {
        UserDTO prof = registerUser("prof", "prof@test.com");

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> profileService.unfollow("unknown", prof.token()));
        assertEquals(ErrorMessages.USERNAME_NOT_FOUND, exception.getMessagesAsString());
    }

    @Test
    void unfollow_throwsAuthenticationException_whenProfileNotFoundForAuthenticatedUser() {
        UserDTO prof = registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        profileRepository.findByUserEmail(prof.email()).ifPresent(profileRepository::delete);

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> profileService.unfollow("celeb", prof.token()));
        assertEquals(ErrorMessages.EMAIL_NOT_FOUND, exception.getMessagesAsString());
    }

}
