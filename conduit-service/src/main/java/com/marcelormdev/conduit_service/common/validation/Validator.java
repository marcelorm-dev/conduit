package com.marcelormdev.conduit_service.common.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.marcelormdev.conduit_service.common.exception.ConduitApiException;

public class Validator {

    private final List<String> violations = new ArrayList<>();

    public Validator notNullOrBlank(String value, String errorMessage) {
        if (value == null || value.isBlank())
            violations.add(errorMessage);

        return this;
    }

    public Validator notBlank(String value, String errorMessage) {
        if (value != null && value.isBlank())
            violations.add(errorMessage);

        return this;
    }

    public Validator emailFormat(String email, String errorMessage) {
        if (email != null && !email.isBlank() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            violations.add(errorMessage);

        return this;
    }

    public void throwViolations(Function<List<String>, ? extends ConduitApiException> factory) {
        if (!violations.isEmpty())
            throw factory.apply(violations);
    }

}
