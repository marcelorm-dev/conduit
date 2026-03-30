package com.marcelormdev.conduit_service.common.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private record BodyResponse(ErrorsResponse errors) {
        private record ErrorsResponse(List<String> body) {

        }

        static BodyResponse of(List<String> errors) {
            return new BodyResponse(new ErrorsResponse(errors));
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public BodyResponse handleAuthenticationException(AuthenticationException ex) {
        return BodyResponse.of(ex.getMessages());
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    @ExceptionHandler(FieldValidationException.class)
    public BodyResponse handleRequestFieldValidationException(FieldValidationException ex) {
        return BodyResponse.of(ex.getMessages());
    }

}
