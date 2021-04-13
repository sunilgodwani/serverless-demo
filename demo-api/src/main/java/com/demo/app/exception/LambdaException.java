package com.demo.app.exception;

import com.demo.app.util.ErrorType;

public class LambdaException extends Exception{

    private final ErrorType errorType;

    public LambdaException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return "LambdaException{" +
                "errorType=" + errorType +
                "errorMessage=" + getMessage() +
                '}';
    }
}
