package com.demo.app.util;

public enum ErrorType {
    MISSING_ACCESS_TOKEN("MISSING_ACCESS_TOKEN", 401),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", 500),
    FORBIDDEN("FORBIDDEN", 403);

    private String errorCode;
    private int statusCode;

    ErrorType(String errorCode, int statusCode) {
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
