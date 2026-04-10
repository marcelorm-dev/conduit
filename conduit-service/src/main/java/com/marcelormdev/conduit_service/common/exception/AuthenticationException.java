package com.marcelormdev.conduit_service.common.exception;

import java.util.List;
import java.util.Map;

public class AuthenticationException extends ConduitApiException {

    private static Map<String, String> mapOfMessages = Map.of(
            ErrorMessages.TOKEN_NOT_INFORMED, ErrorMessages.ACCESS_DENIED_TOKEN_NOT_INFORMED,
            ErrorMessages.TOKEN_INVALID_OR_EXPIRED, ErrorMessages.ACCESS_DENIED_TOKEN_INVALID_OR_EXPIRED);

    public AuthenticationException(String errorMessage) {
        super(mapOfMessages.getOrDefault(errorMessage, errorMessage));
    }

    public AuthenticationException(List<String> errors) {
        super(errors);
    }

}
