package com.demo.app.response;

public class EmptyResponse extends FunctionResponse {

    private String message = "SUCCESS";

    private String value = "";

    public EmptyResponse() {
    }

    public EmptyResponse(String value) {
        this.value = value;
    }

    public EmptyResponse(String message, String value) {
        this.message = message;
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
