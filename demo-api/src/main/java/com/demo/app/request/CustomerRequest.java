package com.demo.app.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerRequest extends FunctionRequest{

    @JsonProperty("email_address")
    private String emailAddress;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
