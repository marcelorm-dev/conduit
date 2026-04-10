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

import com.marcelormdev.conduit_service.auth.AuthService;
import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.profile.ProfileRepository;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    private UserServiceTestHelper userServiceHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void beforeEachTest() {
        userServiceHelper = new UserServiceTestHelper(userService);
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private UserResponse registerUser(String username, String email, String password) {
        return userServiceHelper.registerUser(username, email, password);
    }

    private UserResponse registerUser(String username, String email, String password, String bio, String image) {
        return userServiceHelper.registerUser(username, email, password, bio, image);
    }

    private UserResponse updateUser(String token, Map<String, String> params) {
        return userServiceHelper.updateUser(token, params);
    }

    @Test
    void getCurrentUser_returnsUser_whenTokenIsValid() {
        String token = registerUser("joe", "joe@gmail.com", "123456").user().token();

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
        String invalidToken = "aaaaaaa";

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.currentUser(invalidToken));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED,
                exception.getMessagesAsString());
    }

    @Test
    void getCurrentUser_throwsException_whenTokenBelongsToNonExistentUser() {
        registerUser("joe", "joe@gmail.com", "123456");

        String tokenOfInvalidUser = authService.generateToken("helloworld@gmail.com");

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.currentUser(tokenOfInvalidUser));
        assertEquals(ErrorMessages.ACCESS_DENIED_EMAIL_NOT_FOUND,
                exception.getMessagesAsString());
    }

    @Test
    void login_returnsUser_whenCredentialsAreValid() {
        registerUser("joe", "joe@gmail.com", "123456");

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
        assertEquals(ErrorMessages.INVALID_EMAIL, exception.getMessagesAsString());
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
        registerUser("joe", "joe@gmail.com", "123456");

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.login(new LoginUserRequest("joee@gmail.com", "123456")));
        assertEquals(ErrorMessages.EMAIL_NOT_FOUND, exception.getMessagesAsString());
    }

    @Test
    void login_throwsException_whenPasswordIsWrong() {
        registerUser("joe", "joe@gmail.com", "123456");

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.login(new LoginUserRequest("joe@gmail.com", "aaaaaa")));
        assertEquals(ErrorMessages.INVALID_PASSWORD,
                exception.getMessagesAsString());
    }

    @Test
    void register_returnsNewUser_whenDatasAreValids() {
        assertEquals(0, userRepository.count());

        UserResponse response = registerUser("joe", "joe@gmail.com", "123456");

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
                        registerUser(nullOrBlank, "joe@gmail.com", "123456");
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
                    () -> {
                        registerUser("joe", "joe@gmail.com", nullOrBlank);
                    });
            assertEquals(ErrorMessages.PASSWORD_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void register_throwsException_whenEmailIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> {
                        registerUser("joe", nullOrBlank, "123456");
                    });
            assertEquals(ErrorMessages.EMAIL_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void register_throwsException_whenEmailIsntFormatted() {
        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> registerUser("joe", "blabla@gmailcom", "123456"));
        assertEquals(ErrorMessages.INVALID_EMAIL, exception.getMessagesAsString());
    }

    @Test
    void register_normalizesStringBioAndImageToNull_whenValuesAreEmpty() {
        UserResponse response = registerUser("joe", "joe@gmail.com", "123456", "", "");

        assertNull(response.user().bio());
        assertNull(response.user().image());
    }

    @Test
    void register_throwsException_whenEmailIsAlreadyTaken() {
        registerUser("joe", "joe@gmail.com", "123456");

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> {
                    registerUser("joe2", "joe@gmail.com", "654321");
                });

        assertEquals(ErrorMessages.EMAIL_IS_ALREADY_BEING_USED,
                exception.getMessagesAsString());
    }

    @Test
    void update_returnsUpdatedUser_whenDatasAreValids() {
        String token = registerUser("joe", "joe@gmail.com", "123456").user().token();

        UserResponse response = updateUser(token,
                Map.of("username", "joeupdated",
                        "email", "joe@gmail.com",
                        "password", "newpassword",
                        "bio", "bio",
                        "image", "image"));

        assertEquals("joe@gmail.com", response.user().email());
        assertEquals("joeupdated", response.user().username());
        assertEquals("bio", response.user().bio());
        assertEquals("image", response.user().image());
        assertEquals(token, response.user().token());
    }

    @Test
    void update_generatesNewToken_whenEmailIsUpdated() {
        String token = registerUser("joe", "joe@gmail.com", "123456", "Some bio", "Some image").user().token();
        UserResponse response = updateUser(token, Map.of("email", "newemail@gmail.com"));

        assertEquals("newemail@gmail.com", response.user().email());
        assertEquals("joe", response.user().username());
        assertEquals("Some bio", response.user().bio());
        assertEquals("Some image", response.user().image());
        assertNotEquals(token, response.user().token());

    }

    @Test
    void update_returnsUpdatedBio_whenOnlyBioIsSent() {
        String token = registerUser("joe", "joe@gmail.com", "123456").user().token();

        UserResponse response = updateUser(token, Map.of("bio", "Updated bio"));

        assertEquals("joe@gmail.com", response.user().email());
        assertEquals("joe", response.user().username());
        assertEquals("Updated bio", response.user().bio());
        assertNull(response.user().image());
    }

    @Test
    void update_returnsUpdatedImage_whenOnlyImageIsSent() {
        String token = registerUser("joe", "joe@gmail.com", "123456").user().token();

        UserResponse response = updateUser(token, Map.of("image", "https://example.com/photo.jpg"));

        assertEquals("joe@gmail.com", response.user().email());
        assertEquals("joe", response.user().username());
        assertEquals("https://example.com/photo.jpg", response.user().image());
        assertNull(response.user().bio());
    }

    @Test
    void update_normalizesStringBioAndImageToNull_whenValuesAreEmpty() {
        String token = registerUser("joe", "joe@gmail.com", "123456", "Some bio", "https://example.com/photo.jpg")
                .user().token();

        UserResponse response = updateUser(token, Map.of("bio", "", "image", ""));

        assertNull(response.user().bio());
        assertNull(response.user().image());
    }

    @Test
    void update_clearsBioAndImage_whenNullIsSent() {
        String token = registerUser("joe", "joe@gmail.com", "123456", "Some bio", "https://example.com/photo.jpg")
                .user().token();

        Map<String, String> params = new HashMap<>();
        params.put("bio", null);
        params.put("image", null);

        UserResponse response = updateUser(token, params);

        assertNull(response.user().bio());
        assertNull(response.user().image());
    }

    @Test
    void update_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlanks = new String[] { null, " " };

        for (String nullOrBlank : nullOrBlanks) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> updateUser(nullOrBlank, Map.of("username", "joeupdated")));
            assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_NOT_INFORMED,
                    exception.getMessagesAsString());
        }
    }

    @Test
    void update_throwsException_whenTokenIsInvalid() {
        String invalidToken = "aaaaaaa";

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> updateUser(invalidToken, Map.of("username", "joeupdated")));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED,
                exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenUsernameIsBlank() {
        String token = registerUser("joe", "joe@gmail.com", "123456").user().token();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> updateUser(token, Map.of("username", "")));
        assertEquals(ErrorMessages.USERNAME_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenPasswordIsBlank() {
        String token = registerUser("joe", "joe@gmail.com", "123456").user().token();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> updateUser(token, Map.of("password", "")));
        assertEquals(ErrorMessages.PASSWORD_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenEmailIsBlank() {
        String token = registerUser("joe", "joe@gmail.com", "123456").user().token();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> updateUser(token, Map.of("email", "")));
        assertEquals(ErrorMessages.EMAIL_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenEmailIsntFormatted() {
        String token = registerUser("joe", "joe@gmail.com", "123456").user().token();

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> updateUser(token, Map.of("email", "blabla@gmailcom")));
        assertEquals(ErrorMessages.INVALID_EMAIL, exception.getMessagesAsString());
    }

    @Test
    void getAllUsers_returnsAllUsers_whenTokenIsValid() {
        String token = registerUser("joe", "joe@gmail.com", "123456").user().token();
        registerUser("jane", "jane@gmail.com", "password123");

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
        String invalidToken = "aaaaaaa";

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.getAllUsers(invalidToken));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED,
                exception.getMessagesAsString());
    }

    @Test
    void renewToken_returnsUser_whenTokenIsStillValid() {
        String originalToken = registerUser("joe", "joe@gmail.com", "123456").user().token();

        String renewedToken = userService.renewToken("joe@gmail.com").user().token();

        assertEquals(originalToken, renewedToken);
    }

    @Test
    void renewToken_generatesNewToken_whenStoredTokenIsInvalid() {
        registerUser("joe", "joe@gmail.com", "123456");

        User user = userRepository.findByEmail("joe@gmail.com").get();
        user.setToken("invalid-token");
        userRepository.save(user);

        UserResponse response = userService.renewToken("joe@gmail.com");

        assertNotEquals("invalid-token", response.user().token());
        assertNotNull(response.user().token());
        assertNotEquals("invalid-token", response.user().token());
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
