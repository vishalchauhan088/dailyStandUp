package com.vishalchauhan0688.dailyStandUp.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class BadRequestException extends RuntimeException {
    private final Map<String, Object> details;

    public BadRequestException(String message) {
        super(message);
        this.details = null;
    }

    public BadRequestException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }
}
