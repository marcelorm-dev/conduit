package com.marcelormdev.conduit_service.common.exception;

import java.util.List;

public class FieldValidationException extends ConduitApiException {

    public FieldValidationException(String errorMessage) {
        super(errorMessage);
    }

    public FieldValidationException(List<String> errors) {
        super(errors);
    }

}
