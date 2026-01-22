package com.vishalchauhan0688.dailyStandUp.exception;

public class ResourceNotFoundException extends Exception{
    int code;
    public ResourceNotFoundException(String message, int code){
        super(message);
        this.code = code;
    }
}

// Contributing more in this