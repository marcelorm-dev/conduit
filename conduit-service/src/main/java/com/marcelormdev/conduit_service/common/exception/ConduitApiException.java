package com.marcelormdev.conduit_service.common.exception;

import java.util.List;

public class ConduitApiException extends RuntimeException {

    private final List<String> messages;

    public ConduitApiException(String errorMessage) {
        this(List.of(errorMessage));
    }

    public ConduitApiException(List<String> errors) {
        super(errors.toString());
        this.messages = errors;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getMessagesAsString() {
        return String.join(", ", this.messages);
    }

}
