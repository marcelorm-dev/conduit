package com.marcelormdev.conduit_service.common.exception;

public class ForbiddenException extends ConduitApiException {

    public ForbiddenException(String errorMessage) {
        super(errorMessage);
    }

}
