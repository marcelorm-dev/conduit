package com.marcelormdev.conduit_service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.security.JwtTokenService;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void beforeEachTest() {
        userRepository.deleteAll();
    }

    @Test
    void getCurrentUser_returnsUser_whenTokenIsValid() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        String token = joe.token();
        UserDTO userDTO = userService.currentUser(token);

        assertEquals("joe@gmail.com", userDTO.email());
        assertEquals("123456", userDTO.password());
        assertEquals("joe", userDTO.username());
        assertNull(userDTO.bio());
        assertNull(userDTO.image());
    }

    @Test
    void getCurrentUser_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlankTokens = new String[] { null, " " };

        for (String nullOrBlankToken : nullOrBlankTokens) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> userService.currentUser(nullOrBlankToken));
            assertEquals(ErrorMessages.TOKEN_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void getCurrentUser_throwsException_whenTokenIsInvalid() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        String invalidToken = joe.token() + "aaaaaaa";

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.currentUser(invalidToken));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void getCurrentUser_throwsException_whenTokenBelongsToNonExistentUser() {
        userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        String tokenOfInvalidUser = jwtTokenService.generateToken("helloworld@gmail.com");

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.currentUser(tokenOfInvalidUser));
        assertEquals(ErrorMessages.ACCESS_DENIED, exception.getMessagesAsString());
    }

    @Test
    void login_returnsUser_whenCredentialsAreValid() {
        userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        UserDTO userDTO = userService.login("joe@gmail.com", "123456");

        assertEquals("joe@gmail.com", userDTO.email());
        assertEquals("123456", userDTO.password());
        assertEquals("joe", userDTO.username());
        assertNull(userDTO.bio());
        assertNull(userDTO.image());
    }

    @Test
    void login_throwsException_whenEmailIsNullOrBlank() {
        String[] nullOrBlankEmails = new String[] { null, " " };

        for (String nullOrBlankEmail : nullOrBlankEmails) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> userService.login(nullOrBlankEmail, "123456"));
            assertEquals(ErrorMessages.EMAIL_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void login_throwsException_whenEmailIsntFormatted() {
        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.login("blabla@gmailcom", "123456"));
        assertEquals(ErrorMessages.INVALID_EMAIL, exception.getMessagesAsString());
    }

    @Test
    void login_throwsException_whenPasswordIsNullOrBlank() {
        String[] nullOrBlankPasswords = new String[] { null, " " };

        for (String nullOrBlankPassword : nullOrBlankPasswords) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> userService.login("joe@gmail.com", nullOrBlankPassword));
            assertEquals(ErrorMessages.PASSWORD_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void login_throwsException_whenEmailIsntFoundInDatabase() {
        userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.login("joee@gmail.com", "123456"));
        assertEquals(ErrorMessages.EMAIL_NOT_FOUND, exception.getMessagesAsString());
    }

    @Test
    void login_throwsException_whenPasswordIsWrong() {
        userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.login("joe@gmail.com", "aaaaaa"));
        assertEquals(ErrorMessages.INVALID_PASSWORD, exception.getMessagesAsString());
    }

    @Test
    void register_returnsNewUser_whenDatasAreValids() {
        assertEquals(0, userRepository.count());

        UserDTO userDTO = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        assertEquals("joe@gmail.com", userDTO.email());
        assertEquals("123456", userDTO.password());
        assertEquals("joe", userDTO.username());
        assertNotNull(userDTO.token());
        assertNull(userDTO.bio());
        assertNull(userDTO.image());

        assertEquals(1, userRepository.count());

    }

    @Test
    void register_throwsException_whenUsernameIsNullOrBlank() {
        String[] nullOrBlankUsernames = new String[] { null, " " };

        for (String nullOrBlankUsername : nullOrBlankUsernames) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> userService
                            .register(new UserDTO("joe@gmail.com", "123456", nullOrBlankUsername, null, null, null)));
            assertEquals(ErrorMessages.USERNAME_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void register_throwsException_whenPasswordIsNullOrBlank() {
        String[] nullOrBlankPasswords = new String[] { null, " " };

        for (String nullOrBlankPassword : nullOrBlankPasswords) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> userService
                            .register(new UserDTO("joe@gmail.com", nullOrBlankPassword, "joe", null, null, null)));
            assertEquals(ErrorMessages.PASSWORD_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void register_throwsException_whenEmailIsNullOrBlank() {
        String[] nullOrBlankEmails = new String[] { null, " " };

        for (String nullOrBlankEmail : nullOrBlankEmails) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> userService.register(new UserDTO(nullOrBlankEmail, "123456", "joe", null, null, null)));
            assertEquals(ErrorMessages.EMAIL_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void register_throwsException_whenEmailIsntFormatted() {
        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.register(new UserDTO("blabla@gmailcom", "123456", "joe", null, null, null)));
        assertEquals(ErrorMessages.INVALID_EMAIL, exception.getMessagesAsString());
    }

    @Test
    void register_throwsException_whenEmailIsAlreadyTaken() {
        userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.register(new UserDTO("joe@gmail.com", "654321", "joe2", null, null, null)));

        assertEquals(ErrorMessages.EMAIL_IS_ALREADY_BEING_USED, exception.getMessagesAsString());
    }

    @Test
    void update_returnsUpdatedUser_whenDatasAreValids() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        UserDTO updated = userService.update(joe.token(),
                new UserDTO("joe@gmail.com", "newpassword", "joeupdated", "bio", "image", null));

        assertEquals("joe@gmail.com", updated.email());
        assertEquals("newpassword", updated.password());
        assertEquals("joeupdated", updated.username());
        assertEquals("bio", updated.bio());
        assertEquals("image", updated.image());
        assertEquals(joe.token(), updated.token());
    }

    @Test
    void update_generatesNewToken_whenEmailIsUpdated() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        UserDTO updated = userService.update(joe.token(),
                new UserDTO("newemail@gmail.com", null, null, null, null, null));

        assertEquals("newemail@gmail.com", updated.email());
        assertNotEquals(joe.token(), updated.token());
    }

    @Test
    void update_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlankTokens = new String[] { null, " " };

        for (String nullOrBlankToken : nullOrBlankTokens) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> userService.update(nullOrBlankToken,
                            new UserDTO("joe@gmail.com", "123456", "joe", null, null, null)));
            assertEquals(ErrorMessages.TOKEN_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void update_throwsException_whenTokenIsInvalid() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        String invalidToken = joe.token() + "aaaaaaa";

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.update(invalidToken,
                        new UserDTO("joe@gmail.com", "123456", "joe", null, null, null)));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenUsernameIsBlank() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.update(joe.token(),
                        new UserDTO("joe@gmail.com", "123456", " ", null, null, null)));
        assertEquals(ErrorMessages.USERNAME_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenPasswordIsBlank() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.update(joe.token(),
                        new UserDTO("joe@gmail.com", " ", "joe", null, null, null)));
        assertEquals(ErrorMessages.PASSWORD_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenEmailIsBlank() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.update(joe.token(),
                        new UserDTO(" ", "123456", "joe", null, null, null)));
        assertEquals(ErrorMessages.EMAIL_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenEmailIsntFormatted() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.update(joe.token(),
                        new UserDTO("blabla@gmailcom", "123456", "joe", null, null, null)));
        assertEquals(ErrorMessages.INVALID_EMAIL, exception.getMessagesAsString());
    }

    @Test
    void getAllUsers_returnsAllUsers_whenTokenIsValid() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));
        userService.register(new UserDTO("jane@gmail.com", "123456", "jane", null, null, null));

        List<UserDTO> users = userService.getAllUsers(joe.token());

        assertEquals(2, users.size());
    }

    @Test
    void getAllUsers_throwsException_whenTokenIsNullOrBlank() {
        String[] nullOrBlankTokens = new String[] { null, " " };

        for (String nullOrBlankToken : nullOrBlankTokens) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> userService.getAllUsers(nullOrBlankToken));
            assertEquals(ErrorMessages.TOKEN_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void getAllUsers_throwsException_whenTokenIsInvalid() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        String invalidToken = joe.token() + "aaaaaaa";

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.getAllUsers(invalidToken));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void renewToken_returnsUser_whenTokenIsStillValid() {
        UserDTO joe = userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        UserDTO renewed = userService.renewToken("joe@gmail.com");

        assertEquals(joe.token(), renewed.token());
    }

    @Test
    void renewToken_generatesNewToken_whenStoredTokenIsInvalid() {
        userService.register(new UserDTO("joe@gmail.com", "123456", "joe", null, null, null));

        User user = userRepository.findByEmail("joe@gmail.com").get();
        user.setToken("invalid-token");
        userRepository.save(user);

        UserDTO renewed = userService.renewToken("joe@gmail.com");

        assertNotEquals("invalid-token", renewed.token());
        assertNotNull(renewed.token());
    }

    @Test
    void renewToken_throwsException_whenEmailIsNullOrBlank() {
        String[] nullOrBlankEmails = new String[] { null, " " };

        for (String nullOrBlankEmail : nullOrBlankEmails) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> userService.renewToken(nullOrBlankEmail));
            assertEquals(ErrorMessages.EMAIL_NOT_INFORMED, exception.getMessagesAsString());
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
