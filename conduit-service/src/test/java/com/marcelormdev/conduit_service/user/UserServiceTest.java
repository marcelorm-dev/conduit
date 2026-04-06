package com.marcelormdev.conduit_service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.common.security.JwtTokenService;
import com.marcelormdev.conduit_service.commons.JsonToMapConverter;
import com.marcelormdev.conduit_service.profile.ProfileRepository;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private JwtTokenService jwtTokenService;

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
                        "password": "123456"
                    }
                }
                """.formatted(username, email)));
    }

    @Test
    void getCurrentUser_returnsUser_whenTokenIsValid() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        UserDTO userDTO = userService.currentUser(joe.token());

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
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        String invalidToken = joe.token() + "aaaaaaa";

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.currentUser(invalidToken));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void getCurrentUser_throwsException_whenTokenBelongsToNonExistentUser() {
        registerUser("joe", "joe@gmail.com");

        String tokenOfInvalidUser = jwtTokenService.generateToken("helloworld@gmail.com");

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.currentUser(tokenOfInvalidUser));
        assertEquals(ErrorMessages.ACCESS_DENIED, exception.getMessagesAsString());
    }

    @Test
    void login_returnsUser_whenCredentialsAreValid() {
        registerUser("joe", "joe@gmail.com");

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
        registerUser("joe", "joe@gmail.com");

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.login("joee@gmail.com", "123456"));
        assertEquals(ErrorMessages.EMAIL_NOT_FOUND, exception.getMessagesAsString());
    }

    @Test
    void login_throwsException_whenPasswordIsWrong() {
        registerUser("joe", "joe@gmail.com");

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.login("joe@gmail.com", "aaaaaa"));
        assertEquals(ErrorMessages.INVALID_PASSWORD, exception.getMessagesAsString());
    }

    @Test
    void register_returnsNewUser_whenDatasAreValids() {
        assertEquals(0, userRepository.count());

        UserDTO newUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "123456",
                        "username" : "joe"
                    }
                }
                """);

        UserDTO userDTO = userService.register(newUserDTO);

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
        String[] jsons = new String[] {
                """
                        {
                            "user": {
                                "email" : "joe@gmail.com",
                                "password" : "123456",
                                "username" : null
                            }
                        }
                        """,
                """
                        {
                            "user": {
                                "email" : "joe@gmail.com",
                                "password" : "123456",
                                "username" : " "
                            }
                        }
                        """
        };

        for (String json : jsons) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> {
                        UserDTO newUserDTO = jsonToUserDTO(json);
                        userService.register(newUserDTO);
                    });
            assertEquals(ErrorMessages.USERNAME_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void register_throwsException_whenPasswordIsNullOrBlank() {
        String[] jsons = new String[] {
                """
                        {
                            "user": {
                                "email" : "joe@gmail.com",
                                "password" : null,
                                "username" : "joe"
                            }
                        }
                        """,
                """
                        {
                            "user": {
                                "email" : "joe@gmail.com",
                                "password" : " ",
                                "username" : "joe"
                            }
                        }
                        """
        };

        for (String json : jsons) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> {
                        UserDTO newUserDTO = jsonToUserDTO(json);
                        userService.register(newUserDTO);
                    });
            assertEquals(ErrorMessages.PASSWORD_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void register_throwsException_whenEmailIsNullOrBlank() {
        String[] jsons = new String[] {
                """
                        {
                            "user": {
                                "email" : null,
                                "password" : "123456",
                                "username" : "joe"
                            }
                        }
                        """,
                """
                        {
                            "user": {
                                "email" : " ",
                                "password" : "123456",
                                "username" : "joe"
                            }
                        }
                        """
        };

        for (String json : jsons) {
            FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                    () -> {
                        UserDTO newUserDTO = jsonToUserDTO(json);
                        userService.register(newUserDTO);
                    });
            assertEquals(ErrorMessages.EMAIL_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void register_throwsException_whenEmailIsntFormatted() {
        UserDTO newUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "blabla@gmailcom",
                        "password" : "123456",
                        "username" : "joe"
                    }
                }
                """);

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.register(newUserDTO));
        assertEquals(ErrorMessages.INVALID_EMAIL, exception.getMessagesAsString());
    }

    @Test
    void register_normalizesStringBioToNull_whenStringBioIsEmpty() {
        UserDTO newUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "123456",
                        "username" : "joe",
                        "bio" : ""
                    }
                }
                """);

        UserDTO userDTO = userService.register(newUserDTO);

        assertNull(userDTO.bio());
    }

    @Test
    void register_normalizesStringImageToNull_whenStringImageIsEmpty() {
        UserDTO newUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "123456",
                        "username" : "joe",
                        "image" : ""
                    }
                }
                """);

        UserDTO userDTO = userService.register(newUserDTO);

        assertNull(userDTO.image());
    }

    @Test
    void register_throwsException_whenEmailIsAlreadyTaken() {
        UserDTO newUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "123456",
                        "username" : "joe"
                    }
                }
                """);

        userService.register(newUserDTO);

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> {
                    UserDTO sameEmailUserDTO = jsonToUserDTO("""
                            {
                                "user": {
                                    "email" : "joe@gmail.com",
                                    "password" : "654321",
                                    "username" : "joe2"
                                }
                            }
                            """);
                    userService.register(sameEmailUserDTO);
                });

        assertEquals(ErrorMessages.EMAIL_IS_ALREADY_BEING_USED, exception.getMessagesAsString());
    }

    @Test
    void update_returnsUpdatedUser_whenDatasAreValids() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "newpassword",
                        "username" : "joeupdated",
                        "bio" : "bio",
                        "image" : "image"
                    }
                }
                """);

        UserDTO updated = userService.update(joe.token(), updatedUserDTO);

        assertEquals("joe@gmail.com", updated.email());
        assertEquals("newpassword", updated.password());
        assertEquals("joeupdated", updated.username());
        assertEquals("bio", updated.bio());
        assertEquals("image", updated.image());
        assertEquals(joe.token(), updated.token());
    }

    @Test
    void update_generatesNewToken_whenEmailIsUpdated() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "newemail@gmail.com"
                    }
                }
                """);

        UserDTO updated = userService.update(joe.token(), updatedUserDTO);

        assertEquals("newemail@gmail.com", updated.email());
        assertNotEquals(joe.token(), updated.token());
    }

    @Test
    void update_returnsUpdatedBio_whenOnlyBioIsSent() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "bio" : "Updated bio"
                    }
                }
                """);

        UserDTO updated = userService.update(joe.token(), updatedUserDTO);

        assertEquals("joe@gmail.com", updated.email());
        assertEquals("joe", updated.username());
        assertEquals("Updated bio", updated.bio());
        assertNull(updated.image());
    }

    @Test
    void update_returnsUpdatedImage_whenOnlyImageIsSent() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "image" : "https://example.com/photo.jpg"
                    }
                }
                """);

        UserDTO updated = userService.update(joe.token(), updatedUserDTO);

        assertEquals("joe@gmail.com", updated.email());
        assertEquals("joe", updated.username());
        assertEquals("https://example.com/photo.jpg", updated.image());
        assertNull(updated.bio());
    }

    @Test
    void update_normalizesStringBioToNull_whenStringBioIsEmpty() {
        UserDTO newUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "123456",
                        "username" : "joe",
                        "bio" : "Some bio"
                    }
                }
                """);

        UserDTO joe = userService.register(newUserDTO);

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "bio" : ""
                    }
                }
                """);

        UserDTO updated = userService.update(joe.token(), updatedUserDTO);

        assertNull(updated.bio());
    }

    @Test
    void update_normalizesStringImageToNull_whenStringImageIsEmpty() {
        UserDTO newUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "123456",
                        "username" : "joe",
                        "image" : "Some image"
                    }
                }
                """);

        UserDTO joe = userService.register(newUserDTO);

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "image" : ""
                    }
                }
                """);

        UserDTO updated = userService.update(joe.token(), updatedUserDTO);

        assertNull(updated.image());
    }

    @Test
    void update_clearsBio_whenNullBioIsSent() {
        UserDTO newUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "123456",
                        "username" : "joe",
                        "bio" : "Some bio"
                    }
                }
                """);

        UserDTO joe = userService.register(newUserDTO);

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "bio" : "Temporary bio"
                    }
                }
                """);

        UserDTO withBio = userService.update(joe.token(), updatedUserDTO);
        assertEquals("Temporary bio", withBio.bio());

        updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "bio" : null
                    }
                }
                """);

        UserDTO cleared = userService.update(joe.token(), updatedUserDTO);
        assertNull(cleared.bio());
    }

    @Test
    void update_clearsImage_whenNullImageIsSent() {
        UserDTO newUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "123456",
                        "username" : "joe",
                        "image" : "https://example.com/photo.jpg"
                    }
                }
                """);

        UserDTO joe = userService.register(newUserDTO);

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "image" : "https://example.com/temp.jpg"
                    }
                }
                """);

        UserDTO withImage = userService.update(joe.token(), updatedUserDTO);
        assertEquals("https://example.com/temp.jpg", withImage.image());

        updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "image" : null
                    }
                }
                """);

        UserDTO cleared = userService.update(joe.token(), updatedUserDTO);
        assertNull(cleared.image());
    }

    @Test
    void update_throwsException_whenTokenIsNullOrBlank() {
        UserDTO userDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "123456",
                        "username" : "joe"
                    }
                }
                """);

        String[] nullOrBlankTokens = new String[] { null, " " };

        for (String nullOrBlankToken : nullOrBlankTokens) {
            AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                    () -> userService.update(nullOrBlankToken, userDTO));
            assertEquals(ErrorMessages.TOKEN_NOT_INFORMED, exception.getMessagesAsString());
        }
    }

    @Test
    void update_throwsException_whenTokenIsInvalid() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        String invalidToken = joe.token() + "aaaaaaa";

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.update(invalidToken, joe));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenUsernameIsBlank() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "123456",
                        "username" : ""
                    }
                }
                """);

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.update(joe.token(), updatedUserDTO));
        assertEquals(ErrorMessages.USERNAME_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenPasswordIsBlank() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "joe@gmail.com",
                        "password" : "",
                        "username" : "joe"
                    }
                }
                """);

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.update(joe.token(), updatedUserDTO));
        assertEquals(ErrorMessages.PASSWORD_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenEmailIsBlank() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "",
                        "password" : "123456",
                        "username" : "joe"
                    }
                }
                """);

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.update(joe.token(), updatedUserDTO));
        assertEquals(ErrorMessages.EMAIL_NOT_INFORMED, exception.getMessagesAsString());
    }

    @Test
    void update_throwsException_whenEmailIsntFormatted() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        UserDTO updatedUserDTO = jsonToUserDTO("""
                {
                    "user": {
                        "email" : "blabla@gmailcom",
                        "password" : "123456",
                        "username" : "joe"
                    }
                }
                """);

        FieldValidationException exception = assertThrowsExactly(FieldValidationException.class,
                () -> userService.update(joe.token(), updatedUserDTO));
        assertEquals(ErrorMessages.INVALID_EMAIL, exception.getMessagesAsString());
    }

    @Test
    void getAllUsers_returnsAllUsers_whenTokenIsValid() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");
        registerUser("jane", "jane@gmail.com");

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
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        String invalidToken = joe.token() + "aaaaaaa";

        AuthenticationException exception = assertThrowsExactly(AuthenticationException.class,
                () -> userService.getAllUsers(invalidToken));
        assertEquals(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED, exception.getMessagesAsString());
    }

    @Test
    void renewToken_returnsUser_whenTokenIsStillValid() {
        UserDTO joe = registerUser("joe", "joe@gmail.com");

        UserDTO renewed = userService.renewToken("joe@gmail.com");

        assertEquals(joe.token(), renewed.token());
    }

    @Test
    void renewToken_generatesNewToken_whenStoredTokenIsInvalid() {
        registerUser("joe", "joe@gmail.com");

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
