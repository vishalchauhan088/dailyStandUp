package com.vishalchauhan0688.dailyStandUp.exception;

public class ResourceNotFoundException extends RuntimeException {
    private final int code;
    
    public ResourceNotFoundException(String message) {
        super(message);
        this.code = 404;
    }
    
    public ResourceNotFoundException(String message, int code) {
        super(message);
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}