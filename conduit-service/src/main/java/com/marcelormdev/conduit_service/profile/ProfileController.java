package com.marcelormdev.conduit_service.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marcelormdev.conduit_service.common.http.AuthorizationHeader;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/api/profiles/{username}")
    public ProfileResponse getProfile(@PathVariable String username, @RequestHeader HttpHeaders headers) {
        String token = new AuthorizationHeader(headers).getToken();
        return profileService.getProfile(username, token);
    }

    @PostMapping("/api/profiles/{username}/follow")
    public ProfileResponse follow(@PathVariable String username, @RequestHeader HttpHeaders headers) {
        String token = new AuthorizationHeader(headers).getToken();
        return profileService.follow(username, token);
    }

    @DeleteMapping("/api/profiles/{username}/follow")
    public ProfileResponse unfollow(@PathVariable String username, @RequestHeader HttpHeaders headers) {
        String token = new AuthorizationHeader(headers).getToken();
        return profileService.unfollow(username, token);
    }

}
