package com.marcelormdev.conduit_service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.helpers.TestHelper;
import com.marcelormdev.conduit_service.profile.ProfileRepository;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private TestHelper helper;

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

    @Test
    void getCurrentUser_returnsUser_whenTokenIsValid() {
        String token = helper.register("joe", "joe@gmail.com", "123456").getToken();

        UserResponse response = userService.currentUser(token);

        assertEquals("joe@gmail.com", response.user().email());
        assertEquals("joe", response.user().username());
        assertNull(response.user().bio());
        assertNull(response.user().image());
        assertEquals(token, response.user().token());
    }

    @Test
    void getCurrentUser_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> userService.currentUser(nullOrBlank));
            assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void getCurrentUser_throwsException_whenTokenIsInvalid() {
        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.currentUser("invalid-token"));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED,
                exception.getMessagesAsString());
    }

    @Test
    void getCurrentUser_throwsException_whenTokenBelongsToNonExistentUser() {
        helper.register("joe", "joe@gmail.com", "123456");

        String tokenOfInvalidUser = helper.generateToken("helloworld@gmail.com");

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.currentUser(tokenOfInvalidUser));
        assertEquals(ErrorMessages.ACCESS_DENIED_EMAIL_NOT_FOUND,
                exception.getMessagesAsString());
    }

    @Test
    void login_returnsUser_whenCredentialsAreValid() {
        helper.register("joe", "joe@gmail.com", "123456");

        UserResponse response = userService.login(new LoginUserRequest("joe@gmail.com", "123456"));

        assertEquals("joe@gmail.com", response.user().email());
        assertEquals("joe", response.user().username());
        assertNull(response.user().bio());
        assertNull(response.user().image());
    }

    @Test
    void login_throwsException_whenEmailIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> userService.login(new LoginUserRequest(nullOrBlank, "123456")));
            assertEquals(ErrorMessages.EMAIL_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void login_throwsException_whenEmailIsntFormatted() {
        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.login(new LoginUserRequest("blabla@gmailcom", "123456")));
        assertEquals(ErrorMessages.INVALID_EMAIL,
                exception.getMessagesAsString());
    }

    @Test
    void login_throwsException_whenPasswordIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> userService.login(new LoginUserRequest("joe@gmail.com", nullOrBlank)));
            assertEquals(ErrorMessages.PASSWORD_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void login_throwsException_whenEmailIsntFoundInDatabase() {
        helper.register("joe", "joe@gmail.com", "123456");

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.login(new LoginUserRequest("joee@gmail.com", "123456")));
        assertEquals(ErrorMessages.EMAIL_NOT_FOUND, exception.getMessagesAsString());
    }

    @Test
    void login_throwsException_whenPasswordIsWrong() {
        helper.register("joe", "joe@gmail.com", "123456");

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.login(new LoginUserRequest("joe@gmail.com", "aaaaaa")));
        assertEquals(ErrorMessages.INVALID_PASSWORD,
                exception.getMessagesAsString());
    }

    @Test
    void register_returnsNewUser_whenDatasAreValids() {
        assertEquals(0, userRepository.count());

        UserResponse response = helper.register("joe", "joe@gmail.com", "123456").getUserResponse();

        assertEquals("joe@gmail.com", response.user().email());
        assertEquals("joe", response.user().username());
        assertNotNull(response.user().token());
        assertNull(response.user().bio());
        assertNull(response.user().image());
        assertEquals(1, userRepository.count());
    }

    @Test
    void register_throwsException_whenUsernameIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> {
                        helper.register(nullOrBlank, "joe@gmail.com", "123456");
                    });
            assertEquals(ErrorMessages.USERNAME_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void register_throwsException_whenPasswordIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> helper.register("joe", "joe@gmail.com", nullOrBlank));
            assertEquals(ErrorMessages.PASSWORD_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void register_throwsException_whenEmailIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> helper.register("joe", nullOrBlank, "123456"));
            assertEquals(ErrorMessages.EMAIL_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void register_throwsException_whenEmailIsntFormatted() {
        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> helper.register("joe", "blabla@gmailcom", "123456"));
        assertEquals(ErrorMessages.INVALID_EMAIL, exception.getMessagesAsString());
    }

    @Test
    void register_normalizesStringBioAndImageToNull_whenValuesAreEmpty() {
        UserResponse response = helper.register("joe", "joe@gmail.com", "123456", "", "").getUserResponse();

        assertNull(response.user().bio());
        assertNull(response.user().image());
    }

    @Test
    void register_throwsException_whenEmailIsAlreadyTaken() {
        helper.register("joe", "joe@gmail.com", "123456");

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> helper.register("joe2", "joe@gmail.com", "654321"));
        assertEquals(ErrorMessages.EMAIL_IS_ALREADY_BEING_USED,
                exception.getMessagesAsString());
    }

    @Test
    void update_returnsUpdatedUser_whenDatasAreValids() {
        String token = helper.register("joe", "joe@gmail.com", "123456").getToken();

        UserResponse response = helper.update(token,
                Map.of("username", "joeupdated",
                        "email", "joe@gmail.com",
                        "password", "newpassword",
                        "bio", "bio",
                        "image", "image"))
                .getUserResponse();

        assertEquals("joe@gmail.com", response.user().email());
        assertEquals("joeupdated", response.user().username());
        assertEquals("bio", response.user().bio());
        assertEquals("image", response.user().image());
        assertEquals(token, response.user().token());
    }

    @Test
    void update_generatesNewToken_whenEmailIsUpdated() {
        String token = helper.register("joe", "joe@gmail.com", "123456", "Some bio", "Some image").getToken();
        UserResponse response = helper.update(token, Map.of("email", "newemail@gmail.com")).getUserResponse();

        assertEquals("newemail@gmail.com", response.user().email());
        assertEquals("joe", response.user().username());
        assertEquals("Some bio", response.user().bio());
        assertEquals("Some image", response.user().image());
        assertNotEquals(token, response.user().token());

    }

    @Test
    void update_returnsUpdatedBio_whenOnlyBioIsSent() {
        String token = helper.register("joe", "joe@gmail.com", "123456").getToken();

        UserResponse response = helper.update(token, Map.of("bio", "Updated bio")).getUserResponse();

        assertEquals("joe@gmail.com", response.user().email());
        assertEquals("joe", response.user().username());
        assertEquals("Updated bio", response.user().bio());
        assertNull(response.user().image());
    }

    @Test
    void update_returnsUpdatedImage_whenOnlyImageIsSent() {
        String token = helper.register("joe", "joe@gmail.com", "123456").getToken();

        UserResponse response = helper.update(token, Map.of("image", "https://example.com/photo.jpg"))
                .getUserResponse();

        assertEquals("joe@gmail.com", response.user().email());
        assertEquals("joe", response.user().username());
        assertEquals("https://example.com/photo.jpg", response.user().image());
        assertNull(response.user().bio());
    }

    @Test
    void update_normalizesStringBioAndImageToNull_whenValuesAreEmpty() {
        String token = helper
                .register("joe", "joe@gmail.com", "123456", "Some bio", "https://example.com/photo.jpg")
                .getToken();

        UserResponse response = helper.update(token, Map.of("bio", "", "image", ""))
                .getUserResponse();

        assertNull(response.user().bio());
        assertNull(response.user().image());
    }

    @Test
    void update_clearsBioAndImage_whenNullIsSent() {
        String token = helper
                .register("joe", "joe@gmail.com", "123456", "Some bio", "https://example.com/photo.jpg")
                .getToken();

        Map<String, String> params = new HashMap<>();
        params.put("bio", null);
        params.put("image", null);

        UserResponse response = helper.update(token, params).getUserResponse();

        assertNull(response.user().bio());
        assertNull(response.user().image());
    }

    @Test
    void update_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> helper.update(nullOrBlank, Map.of("username", "joeupdated")));
            assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void update_throwsException_whenTokenIsInvalid() {
        String invalidToken = "aaaaaaa";

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> helper.update(invalidToken, Map.of("username", "joeupdated")));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED,
                exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenUsernameIsBlank() {
        String token = helper.register("joe", "joe@gmail.com", "123456").getToken();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> helper.update(token, Map.of("username", "")));
        assertEquals(ErrorMessages.USERNAME_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenPasswordIsBlank() {
        String token = helper.register("joe", "joe@gmail.com", "123456").getToken();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> helper.update(token, Map.of("password", "")));
        assertEquals(ErrorMessages.PASSWORD_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenEmailIsBlank() {
        String token = helper.register("joe", "joe@gmail.com", "123456").getToken();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> helper.update(token, Map.of("email", "")));
        assertEquals(ErrorMessages.EMAIL_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenEmailIsntFormatted() {
        String token = helper.register("joe", "joe@gmail.com", "123456").getToken();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> helper.update(token, Map.of("email", "blabla@gmailcom")));
        assertEquals(ErrorMessages.INVALID_EMAIL, exception.getMessagesAsString());
    }

    @Test
    void getAllUsers_returnsAllUsers_whenTokenIsValid() {
        String token = helper.register("joe", "joe@gmail.com", "123456").getToken();
        helper.register("jane", "jane@gmail.com", "password123");

        List<UserResponse> users = userService.getAllUsers(token);

        assertEquals(2, users.size());
    }

    @Test
    void getAllUsers_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> userService.getAllUsers(nullOrBlank));
            assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void getAllUsers_throwsException_whenTokenIsInvalid() {
        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.getAllUsers("invalid-token"));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED,
                exception.getMessagesAsString());
    }

    @Test
    void renewToken_returnsUser_whenTokenIsStillValid() {
        String originalToken = helper.register("joe", "joe@gmail.com", "123456").getToken();

        String renewedToken = userService.renewToken("joe@gmail.com").user().token();

        assertEquals(originalToken, renewedToken);
    }

    @Test
    void renewToken_generatesNewToken_whenStoredTokenIsInvalid() {
        helper.register("joe", "joe@gmail.com", "123456");

        User user = userRepository.findByEmail("joe@gmail.com").get();
        user.setToken("invalid-token");
        userRepository.save(user);

        UserResponse response = userService.renewToken("joe@gmail.com");

        assertNotEquals("invalid-token", response.user().token());
        assertNotNull(response.user().token());
    }

    @Test
    void renewToken_throwsException_whenEmailIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> userService.renewToken(nullOrBlank));
            assertEquals(ErrorMessages.EMAIL_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void renewToken_throwsException_whenEmailIsntFormatted() {
        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.renewToken("blabla@gmailcom"));
        assertEquals(ErrorMessages.INVALID_EMAIL, exception.getMessagesAsString());
    }

    @Test
    void renewToken_throwsException_whenEmailIsntFoundInDatabase() {
        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.renewToken("notfound@gmail.com"));
        assertEquals(ErrorMessages.EMAIL_NOT_FOUND, exception.getMessagesAsString());
    }

}
