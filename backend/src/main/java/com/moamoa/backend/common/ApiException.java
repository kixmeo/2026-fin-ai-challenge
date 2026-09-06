package com.moamoa.backend.common;

import java.util.Objects;

public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(Objects.requireNonNull(errorCode, "errorCode must not be null").getDefaultMessage());
        this.errorCode = errorCode;
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(requireNonBlank(message));
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    public ApiException(ErrorCode errorCode, String message, Throwable cause) {
        super(requireNonBlank(message), cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    private static String requireNonBlank(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        return message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
