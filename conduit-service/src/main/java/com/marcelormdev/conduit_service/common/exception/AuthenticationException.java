package com.marcelormdev.conduit_service.common.exception;

import java.util.List;

public class AuthenticationException extends ConduitApiException {

    public AuthenticationException(String errorMessage) {
        super(errorMessage);
    }

    public AuthenticationException(List<String> errors) {
        super(errors);
    }

}
