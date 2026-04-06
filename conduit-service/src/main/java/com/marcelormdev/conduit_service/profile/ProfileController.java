package com.marcelormdev.conduit_service.profile;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marcelormdev.conduit_service.http.JwtAuthorizationHeader;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {

    private final ProfileService profileService;

    ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    private record BodyResponse(ProfileResponse profile) {

        private record ProfileResponse(String username, String bio, String image, boolean following) {
        }

        static BodyResponse of(ProfileDTO profileDTO) {
            return new BodyResponse(new ProfileResponse(
                    profileDTO.username(),
                    profileDTO.bio(),
                    profileDTO.image(),
                    profileDTO.following()));
        }
    }

    @GetMapping("/api/profiles/{username}")
    public BodyResponse getProfile(@PathVariable String username, @RequestHeader HttpHeaders headers) {
        String token = new JwtAuthorizationHeader(headers).getToken();
        return BodyResponse.of(profileService.getProfile(username, token));
    }

    @PostMapping("/api/profiles/{username}/follow")
    public BodyResponse follow(@PathVariable String username, @RequestHeader HttpHeaders headers) {
        String token = new JwtAuthorizationHeader(headers).getToken();
        return BodyResponse.of(profileService.follow(username, token));
    }

    @DeleteMapping("/api/profiles/{username}/follow")
    public BodyResponse unfollow(@PathVariable String username, @RequestHeader HttpHeaders headers) {
        String token = new JwtAuthorizationHeader(headers).getToken();
        return BodyResponse.of(profileService.unfollow(username, token));
    }

}
