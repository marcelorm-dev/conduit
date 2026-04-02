package com.marcelormdev.conduit_service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.marcelormdev.conduit_service.apitest.ControllerTest;
import com.marcelormdev.conduit_service.apitest.UserRestCaller;

class UserControllerTest extends ControllerTest {

    @Autowired
    private UserRepository userRepository;

    private UserRestCaller restCaller;

    @BeforeEach
    void beforeEachTest() {
        userRepository.deleteAll();
        restCaller = new UserRestCaller(restClient);
    }

    @Test
    void login_returnsUserData_whenCredentialsAreValid() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "I like barbecue"
                            }
                        }
                """)
                .expectStatus().isCreated();

        restCaller.callLoginAPI("""
                        {
                            "user":{
                                "email": "jake@hotmail.com",
                                "password": "123456"
                            }
                        }
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.password").doesNotExist()
                .jsonPath("$.user.email").isEqualTo("jake@hotmail.com")
                .jsonPath("$.user.username").isEqualTo("Jacob")
                .jsonPath("$.user.bio").isEqualTo("I like barbecue")
                .jsonPath("$.user.token").isNotEmpty()
                .jsonPath("$.user.image").isEqualTo(null);
    }

    @Test
    void login_returns422_whenPasswordIsWrong() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "I like barbecue"
                            }
                        }
                """)
                .expectStatus().isCreated();

        restCaller.callLoginAPI("""
                        {
                            "user":{
                                "email": "jake@hotmail.com",
                                "password": "1111"
                            }
                        }
                """)
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.errors").isEqualTo("{\"body\":[\"Password is invalid\"]}");
    }

    @Test
    void register_returnsUserData_whenInputIsValid() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "I like barbecue"
                            }
                        }
                """)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.user.password").doesNotExist()
                .jsonPath("$.user.email").isEqualTo("jake@hotmail.com")
                .jsonPath("$.user.username").isEqualTo("Jacob")
                .jsonPath("$.user.bio").isEqualTo("I like barbecue")
                .jsonPath("$.user.token").isNotEmpty()
                .jsonPath("$.user.image").isEqualTo(null);
    }

    @Test
    void register_returns422_whenRequiredFieldsAreMissing() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{

                            }
                        }
                """)
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.errors").isEqualTo(
                        "{\"body\":[\"Username must be informed\",\"Password must be informed\",\"Email must be informed\"]}");
    }

    @Test
    void listUsers_returnsAllUsers_whenTokenIsValid() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "I like barbecue"
                            }
                        }
                """)
                .expectStatus().isCreated();

        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Joe",
                                "email": "joe@hotmail.com",
                                "password": "654321"
                            }
                        }
                """)
                .expectStatus().isCreated();

        String token = jwtTokenService.generateToken("jake@hotmail.com");
        restCaller.callListUsersAPI(token)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].user.email").isEqualTo("jake@hotmail.com")
                .jsonPath("$[0].user.username").isEqualTo("Jacob")
                .jsonPath("$[0].user.bio").isEqualTo("I like barbecue")
                .jsonPath("$[0].user.password").doesNotExist()
                .jsonPath("$[1].user.email").isEqualTo("joe@hotmail.com")
                .jsonPath("$[1].user.username").isEqualTo("Joe")
                .jsonPath("$[1].user.password").doesNotExist();
    }

    @Test
    void listUsers_returns401_whenTokenIsMissing() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "I like barbecue"
                            }
                        }
                """)
                .expectStatus().isCreated();

        restCaller.callListUsersAPI(null)
                .expectStatus().isUnauthorized();
    }

    @Test
    void getCurrentUser_returnsCurrentUser_whenTokenIsValid() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "I like barbecue"
                            }
                        }
                """)
                .expectStatus().isCreated();

        String token = jwtTokenService.generateToken("jake@hotmail.com");
        restCaller.callCurrentUserAPI(token)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.email").isEqualTo("jake@hotmail.com");
    }

    @Test
    void getCurrentUser_returns401_whenTokenIsMissing() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "I like barbecue"
                            }
                        }
                """)
                .expectStatus().isCreated();

        restCaller.callCurrentUserAPI(null)
                .expectStatus().isUnauthorized();
    }

    @Test
    void update_returnsUpdatedUser_whenTokenIsValid() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "I like barbecue"
                            }
                        }
                """)
                .expectStatus().isCreated();

        String token = jwtTokenService.generateToken("jake@hotmail.com");
        restCaller.callUpdateUserAPI(token, """
                        {
                            "user":{
                                "username": "JacobUpdated",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "I like pizza now",
                                "image" : "https://example.com/photo.jpg"
                            }
                        }
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.password").doesNotExist()
                .jsonPath("$.user.email").isEqualTo("jake@hotmail.com")
                .jsonPath("$.user.username").isEqualTo("JacobUpdated")
                .jsonPath("$.user.bio").isEqualTo("I like pizza now")
                .jsonPath("$.user.image").isEqualTo("https://example.com/photo.jpg")
                .jsonPath("$.user.token").isNotEmpty();
    }

    @Test
    void update_normalizesEmptyStringBioToNull() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "Updated bio"

                            }
                        }
                """)
                .expectStatus().isCreated();

        String token = jwtTokenService.generateToken("jake@hotmail.com");

        restCaller.callUpdateUserAPI(token, """
                        {
                            "user":{
                                "bio": ""
                            }
                        }
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.bio").isEqualTo(null);
    }

    @Test
    void update_acceptsNullBio() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "Temporary bio"
                            }
                        }
                """)
                .expectStatus().isCreated();

        String token = jwtTokenService.generateToken("jake@hotmail.com");

        restCaller.callUpdateUserAPI(token, """
                        {
                            "user":{
                                "bio": null
                            }
                        }
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.bio").isEqualTo(null);
    }

    @Test
    void update_updatesImage_whenOnlyImageIsSent() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456"
                            }
                        }
                """)
                .expectStatus().isCreated();

        String token = jwtTokenService.generateToken("jake@hotmail.com");
        restCaller.callUpdateUserAPI(token, """
                        {
                            "user":{
                                "image": "https://example.com/photo.jpg"
                            }
                        }
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.image").isEqualTo("https://example.com/photo.jpg")
                .jsonPath("$.user.bio").isEqualTo(null);
    }

    @Test
    void update_normalizesEmptyStringImageToNull() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456"
                            }
                        }
                """)
                .expectStatus().isCreated();

        String token = jwtTokenService.generateToken("jake@hotmail.com");
        restCaller.callUpdateUserAPI(token, """
                        {
                            "user":{
                                "image": "https://example.com/photo.jpg"
                            }
                        }
                """)
                .expectStatus().isOk();

        restCaller.callUpdateUserAPI(token, """
                        {
                            "user":{
                                "image": ""
                            }
                        }
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.image").isEqualTo(null);
    }

    @Test
    void update_acceptsNullImage() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "image": "https://example.com/temp.jpg"
                            }
                        }
                """)
                .expectStatus().isCreated();

        String token = jwtTokenService.generateToken("jake@hotmail.com");

        restCaller.callUpdateUserAPI(token, """
                        {
                            "user":{
                                "image": null
                            }
                        }
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.image").isEqualTo(null);
    }

    @Test
    void update_persistsUsernameAndEmailChange_afterGetRequestWithNewToken() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456",
                                "bio": "Updated bio"
                            }
                        }
                """)
                .expectStatus().isCreated();

        String originalToken = jwtTokenService.generateToken("jake@hotmail.com");
        restCaller.callUpdateUserAPI(originalToken, """
                        {
                            "user":{
                                "username": "JacobUpdated",
                                "email": "jake_updated@hotmail.com"
                            }
                        }
                """)
                .expectStatus().isOk();

        String newToken = jwtTokenService.generateToken("jake_updated@hotmail.com");
        restCaller.callCurrentUserAPI(newToken)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.username").isEqualTo("JacobUpdated")
                .jsonPath("$.user.email").isEqualTo("jake_updated@hotmail.com")
                .jsonPath("$.user.bio").isEqualTo("Updated bio")
                .jsonPath("$.user.image").isEqualTo(null)
                .jsonPath("$.user.token").isNotEmpty();
    }

    @Test
    void update_generatesNewToken_whenEmailChanges() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456"
                            }
                        }
                """)
                .expectStatus().isCreated();

        String originalToken = jwtTokenService.generateToken("jake@hotmail.com");
        restCaller.callUpdateUserAPI(originalToken, """
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jacob_new@hotmail.com",
                                "password": "123456"
                            }
                        }
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.email").isEqualTo("jacob_new@hotmail.com")
                .jsonPath("$.user.token").value(newToken -> {
                    assert !newToken.equals(originalToken) : "Token should have been regenerated after email change";
                });
    }

    @Test
    void update_returns401_whenTokenIsMissing() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456"
                            }
                        }
                """)
                .expectStatus().isCreated();

        restCaller.callUpdateUserAPI(null, """
                        {
                            "user":{
                                "username": "JacobUpdated",
                                "email": "jake@hotmail.com",
                                "password": "123456"
                            }
                        }
                """)
                .expectStatus().isUnauthorized();
    }

    @Test
    void update_returns422_whenRequiredFieldsAreBlank() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456"
                            }
                        }
                """)
                .expectStatus().isCreated();

        String token = jwtTokenService.generateToken("jake@hotmail.com");
        restCaller.callUpdateUserAPI(token, """
                        {
                            "user":{
                                "username": "",
                                "email": "",
                                "password": ""
                            }
                        }
                """)
                .expectStatus().isEqualTo(422);
    }

    @Test
    void renewToken_returnsUserWithToken_whenEmailExists() {
        restCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "Jacob",
                                "email": "jake@hotmail.com",
                                "password": "123456"
                            }
                        }
                """)
                .expectStatus().isCreated();

        restCaller.callRenewTokenAPI("""
                        {
                            "user":{
                                "email": "jake@hotmail.com"
                            }
                        }
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.password").doesNotExist()
                .jsonPath("$.user.email").isEqualTo("jake@hotmail.com")
                .jsonPath("$.user.username").isEqualTo("Jacob")
                .jsonPath("$.user.token").isNotEmpty();
    }

    @Test
    void renewToken_returns422_whenEmailIsNotFound() {
        restCaller.callRenewTokenAPI("""
                        {
                            "user":{
                                "email": "unknown@hotmail.com"
                            }
                        }
                """)
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.errors").isEqualTo("{\"body\":[\"Email not found\"]}");
    }

    @Test
    void renewToken_returns422_whenEmailIsMissing() {
        restCaller.callRenewTokenAPI("""
                        {
                            "user":{
                            }
                        }
                """)
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.errors").isEqualTo("{\"body\":[\"Email must be informed\"]}");
    }

}
