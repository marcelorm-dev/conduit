package com.marcelormdev.conduit_service.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcelormdev.conduit_service.auth.AuthService;
import com.marcelormdev.conduit_service.common.exception.ErrorMessages;
import com.marcelormdev.conduit_service.common.exception.FieldValidationException;
import com.marcelormdev.conduit_service.common.validation.Validator;

@Service
public class UserService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    public UserResponse currentUser(String token) {
        User user = authService.authenticate(token, userRepository::findByEmail);
        return new UserResponse(user);
    }

    public UserResponse login(LoginUserRequest request) {
        String email = request.user().email();
        String password = request.user().password();

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

        return new UserResponse(user);
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        String username = request.user().username();
        String email = request.user().email();
        String password = request.user().password();

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
                request.user().email(),
                request.user().password(),
                request.user().username(),
                request.user().bio(),
                request.user().image(),
                token);

        user = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user));

        return new UserResponse(user);
    }

    @Transactional
    public UserResponse update(String token, UpdateUserRequest request) {
        User user = authService.authenticate(token, userRepository::findByEmail);

        String username = request.user().username();
        String email = request.user().email();
        String password = request.user().password();

        new Validator()
                .notBlank(username, ErrorMessages.USERNAME_NOT_INFORMED)
                .notBlank(email, ErrorMessages.EMAIL_NOT_INFORMED)
                .notBlank(password, ErrorMessages.PASSWORD_NOT_INFORMED)
                .emailFormat(email, ErrorMessages.INVALID_EMAIL)
                .throwViolations(FieldValidationException::new);

        boolean wasEmailUpdated = email != null && !user.getEmail().equals(email);
        String newToken = wasEmailUpdated ? authService.generateToken(email) : null;

        user.update(
                request.user().username(),
                request.user().email(),
                request.user().password(),
                request.user().bio(),
                request.user().hasBio(),
                request.user().image(),
                request.user().hasImage(),
                newToken);

        user = userRepository.save(user);

        return new UserResponse(user);
    }

    public List<UserResponse> getAllUsers(String token) {
        authService.authenticate(token, userRepository::findByEmail);
        return userRepository.findAll().stream().map(UserResponse::new).toList();
    }

    @Transactional
    public UserResponse renewToken(String email) {
        new Validator()
                .notNullOrBlank(email, ErrorMessages.EMAIL_NOT_INFORMED)
                .emailFormat(email, ErrorMessages.INVALID_EMAIL)
                .throwViolations(FieldValidationException::new);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new FieldValidationException(ErrorMessages.EMAIL_NOT_FOUND));

        if (!authService.isTokenValid(user.getToken()))
            user.setToken(authService.generateToken(email));

        user = userRepository.save(user);

        return new UserResponse(user);
    }

}
