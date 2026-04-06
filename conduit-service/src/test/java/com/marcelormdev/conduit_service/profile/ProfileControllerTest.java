package com.marcelormdev.conduit_service.profile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.marcelormdev.conduit_service.apitest.ControllerTest;
import com.marcelormdev.conduit_service.apitest.ProfileRestCaller;
import com.marcelormdev.conduit_service.apitest.UserRestCaller;
import com.marcelormdev.conduit_service.user.UserRepository;

class ProfileControllerTest extends ControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private UserRestCaller userRestCaller;
    private ProfileRestCaller profileRestCaller;

    @BeforeEach
    void beforeEachTest() {
        profileRepository.deleteAll();
        userRepository.deleteAll();
        userRestCaller = new UserRestCaller(restClient);
        profileRestCaller = new ProfileRestCaller(restClient);
    }

    private void registerUser(String username, String email) {
        userRestCaller.callRegisterAPI("""
                        {
                            "user":{
                                "username": "%s",
                                "email": "%s",
                                "password": "password123"
                            }
                        }
                """.formatted(username, email))
                .expectStatus().isCreated();
    }

    @Test
    void getProfile_returnsProfileWithFollowingFalse_withoutAuth() {
        registerUser("celeb", "celeb@test.com");

        profileRestCaller.callGetProfileAPI("celeb")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.profile.username").isEqualTo("celeb")
                .jsonPath("$.profile.bio").isEqualTo(null)
                .jsonPath("$.profile.image").isEqualTo(null)
                .jsonPath("$.profile.following").isEqualTo(false);
    }

    @Test
    void getProfile_returnsProfileWithFollowingFalse_withAuth_whenNotFollowing() {
        registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        String token = jwtTokenService.generateToken("prof@test.com");
        profileRestCaller.callGetProfileAPI("celeb", token)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.profile.username").isEqualTo("celeb")
                .jsonPath("$.profile.bio").isEqualTo(null)
                .jsonPath("$.profile.image").isEqualTo(null)
                .jsonPath("$.profile.following").isEqualTo(false);
    }

    @Test
    void getProfile_returns422_whenUsernameNotFound() {
        profileRestCaller.callGetProfileAPI("unknown")
                .expectStatus().isEqualTo(422);
    }

    @Test
    void follow_returnsProfileWithFollowingTrue_whenTokenIsValid() {
        registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        String token = jwtTokenService.generateToken("prof@test.com");
        profileRestCaller.callFollowAPI("celeb", token)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.profile.username").isEqualTo("celeb")
                .jsonPath("$.profile.bio").isEqualTo(null)
                .jsonPath("$.profile.image").isEqualTo(null)
                .jsonPath("$.profile.following").isEqualTo(true);
    }

    @Test
    void follow_returns401_whenTokenIsMissing() {
        registerUser("celeb", "celeb@test.com");

        profileRestCaller.callFollowAPI("celeb", null)
                .expectStatus().isUnauthorized();
    }

    @Test
    void unfollow_returnsProfileWithFollowingFalse_whenTokenIsValid() {
        registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        String token = jwtTokenService.generateToken("prof@test.com");
        profileRestCaller.callFollowAPI("celeb", token);

        profileRestCaller.callUnfollowAPI("celeb", token)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.profile.username").isEqualTo("celeb")
                .jsonPath("$.profile.bio").isEqualTo(null)
                .jsonPath("$.profile.image").isEqualTo(null)
                .jsonPath("$.profile.following").isEqualTo(false);
    }

    @Test
    void unfollow_returns401_whenTokenIsMissing() {
        registerUser("celeb", "celeb@test.com");

        profileRestCaller.callUnfollowAPI("celeb", null)
                .expectStatus().isUnauthorized();
    }

    @Test
    void getProfile_returnsFollowingFalse_afterUnfollow() {
        registerUser("prof", "prof@test.com");
        registerUser("celeb", "celeb@test.com");

        String token = jwtTokenService.generateToken("prof@test.com");
        profileRestCaller.callFollowAPI("celeb", token);
        profileRestCaller.callUnfollowAPI("celeb", token);

        profileRestCaller.callGetProfileAPI("celeb", token)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.profile.username").isEqualTo("celeb")
                .jsonPath("$.profile.bio").isEqualTo(null)
                .jsonPath("$.profile.image").isEqualTo(null)
                .jsonPath("$.profile.following").isEqualTo(false);
    }

}
