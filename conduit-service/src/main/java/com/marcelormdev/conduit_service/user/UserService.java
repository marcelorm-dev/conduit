package com.marcelormdev.conduit_service.user;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcelormdev.conduit_service.common.exception.AuthenticationException;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.common.validation.Validator;
import com.marcelormdev.conduit_service.profile.Profile;
import com.marcelormdev.conduit_service.profile.ProfileRepository;
import com.marcelormdev.conduit_service.security.JwtTokenService;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final JwtTokenService jwtTokenService;

    UserService(UserRepository userRepository, ProfileRepository profileRepository, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.jwtTokenService = jwtTokenService;
    }

    private User authenticate(String token) {
        new Validator()
                .notNullOrBlank(token, ErrorMessages.TOKEN_NOT_INFORMED)
                .throwViolations(AuthenticationException::new);

        if (!jwtTokenService.isTokenValid(token)) {
            throw new AuthenticationException(ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED);
        }

        String email = jwtTokenService.extractEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException(ErrorMessages.ACCESS_DENIED));
    }

    public UserDTO currentUser(String token) {
        User user = authenticate(token);
        return new UserDTO(user);
    }

    public UserDTO login(String email, String password) {
        new Validator()
                .notNullOrBlank(email, ErrorMessages.EMAIL_NOT_INFORMED)
                .notNullOrBlank(password, ErrorMessages.PASSWORD_NOT_INFORMED)
                .emailFormat(email, ErrorMessages.INVALID_EMAIL)
                .throwViolations(FieldValidationException::new);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new FieldValidationException(ErrorMessages.EMAIL_NOT_FOUND));

        if (!user.getPassword().equals(password)) {
            throw new FieldValidationException(ErrorMessages.INVALID_PASSWORD);
        }

        return new UserDTO(user);
    }

    @Transactional
    public UserDTO register(UserDTO userDTO) {
        String username = userDTO.username();
        String email = userDTO.email();
        String password = userDTO.password();

        new Validator()
                .notNullOrBlank(username, ErrorMessages.USERNAME_NOT_INFORMED)
                .notNullOrBlank(password, ErrorMessages.PASSWORD_NOT_INFORMED)
                .notNullOrBlank(email, ErrorMessages.EMAIL_NOT_INFORMED)
                .emailFormat(email, ErrorMessages.INVALID_EMAIL)
                .throwViolations(FieldValidationException::new);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent())
            throw new FieldValidationException(ErrorMessages.EMAIL_IS_ALREADY_BEING_USED);

        String token = jwtTokenService.generateToken(email);
        User user = new User(
                userDTO.email(),
                userDTO.password(),
                userDTO.username(),
                userDTO.bio(),
                userDTO.image(),
                token);

        user = userRepository.save(user);
        profileRepository.save(new Profile(user));

        return new UserDTO(user);
    }

    @Transactional
    public UserDTO update(String token, UserDTO userDTO) {
        User user = authenticate(token);

        String username = userDTO.username();
        String email = userDTO.email();
        String password = userDTO.password();

        new Validator()
                .notBlank(username, ErrorMessages.USERNAME_NOT_INFORMED)
                .notBlank(email, ErrorMessages.EMAIL_NOT_INFORMED)
                .notBlank(password, ErrorMessages.PASSWORD_NOT_INFORMED)
                .emailFormat(email, ErrorMessages.INVALID_EMAIL)
                .throwViolations(FieldValidationException::new);

        boolean wasEmailUpdated = email != null && !user.getEmail().equals(email);
        String newToken = wasEmailUpdated ? jwtTokenService.generateToken(email) : null;

        user.update(
                userDTO.username(),
                userDTO.email(),
                userDTO.password(),
                userDTO.hasBio(),
                userDTO.bio(),
                userDTO.hasImage(),
                userDTO.image(),
                newToken);

        user = userRepository.save(user);

        return new UserDTO(user);
    }

    public List<UserDTO> getAllUsers(String token) {
        authenticate(token);
        return userRepository.findAll().stream().map(UserDTO::new).toList();
    }

    @Transactional
    public UserDTO renewToken(String email) {
        new Validator()
                .notNullOrBlank(email, ErrorMessages.EMAIL_NOT_INFORMED)
                .emailFormat(email, ErrorMessages.INVALID_EMAIL)
                .throwViolations(FieldValidationException::new);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new FieldValidationException(ErrorMessages.EMAIL_NOT_FOUND));

        if (!jwtTokenService.isTokenValid(user.getToken()))
            user.setToken(jwtTokenService.generateToken(email));

        user = userRepository.save(user);

        return new UserDTO(user);
    }

}
