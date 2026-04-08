package com.marcelormdev.conduit_service.user;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcelormdev.conduit_service.auth.AuthService;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.common.validation.Validator;

@Service
public class UserService {

    private final ApplicationEventPublisher eventPublisher;

    private final UserRepository userRepository;

    private final AuthService authService;

    UserService(ApplicationEventPublisher eventPublisher, UserRepository userRepository, AuthService authService) {
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public UserDTO currentUser(String token) {
        User user = authService.authenticate(token, userRepository::findByEmail);
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

        if (userRepository.existsByEmail(email))
            throw new FieldValidationException(ErrorMessages.EMAIL_IS_ALREADY_BEING_USED);

        String token = authService.generateToken(email);
        User user = new User(
                userDTO.email(),
                userDTO.password(),
                userDTO.username(),
                userDTO.bio(),
                userDTO.image(),
                token);

        user = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user));

        return new UserDTO(user);
    }

    @Transactional
    public UserDTO update(String token, UserDTO userDTO) {
        User user = authService.authenticate(token, userRepository::findByEmail);

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
        String newToken = wasEmailUpdated ? authService.generateToken(email) : null;

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
        authService.authenticate(token, userRepository::findByEmail);
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

        if (!authService.isTokenValid(user.getToken()))
            user.setToken(authService.generateToken(email));

        user = userRepository.save(user);

        return new UserDTO(user);
    }

}
