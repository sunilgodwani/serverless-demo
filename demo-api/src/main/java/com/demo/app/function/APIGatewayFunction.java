package com.demo.app.function;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.GetUserRequest;
import com.amazonaws.services.cognitoidp.model.GetUserResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.demo.app.response.EmptyResponse;
import com.demo.app.response.ErrorResponse;
import com.demo.app.util.ErrorType;
import com.demo.app.exception.LambdaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class APIGatewayFunction<T, S> implements Function<Message<T>, Message<S>> {

    private Logger LOG = LoggerFactory.getLogger(APIGatewayFunction.class);

    private APIGatewayProxyRequestEvent requestEvent;

    private String requestId;

    @Autowired
    private AWSCognitoIdentityProvider awsCognitoIdentityProvider;

    protected abstract S functionHandler(Message<T> voidMessage);

    @Override
    public Message<S> apply(Message<T> message) {
        try {
            message.getHeaders().keySet().stream().forEach(key -> {LOG.info(appendContext(key));});
            if (message.getHeaders().get("httpMethod") != null) {
                return buildResponse(functionHandler(message));
            } else {
                return getEmptyResponse();
            }
        } catch (Exception ex) {
            LOG.error(appendContext("Error occurred while processing request: "), ex);
            return getErrorResponse(ex.getMessage(), ErrorType.INTERNAL_SERVER_ERROR);
        }
    }

    protected String appendContext(String msg) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("[REQUEST ID] : "+requestId+" :");
        messageBuilder.append(msg);
        return messageBuilder.toString();
    }

    protected Message<S> buildResponse(S response) {
        return getResponseBuilder(response).build();
    }

    private MessageBuilder getResponseBuilder(S response) {
        return MessageBuilder.withPayload(response).copyHeaders(getCORSHeaders());
    }

    private Map<String, String> getCORSHeaders() {
        Map<String, String> corsHeaders = new HashMap<>();

        corsHeaders.put("Access-Control-Allow-Origin", "*");
        corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,access_token,Authorization,X-Api-Key,X-Amz-Security-Token,X-Amz-User-Agent");
        corsHeaders.put("Access-Control-Allow-Methods", "OPTIONS,GET,PUT,POST,DELETE");

        return corsHeaders;
    }

    private Message<S> getEmptyResponse() {
        EmptyResponse response = new EmptyResponse("Warmed UP");
        return getResponseBuilder((S)response).setHeader("statuscode", 200).build();
    }

    protected Message<S> getErrorResponse(LambdaException ex) {
        ErrorResponse response = new ErrorResponse(ex.getErrorType().getErrorCode(), ex.getMessage());
        return getResponseBuilder((S)response).setHeader("statuscode", ex.getErrorType().getStatusCode()).build();
    }

    protected Message<S> getErrorResponse(String message, ErrorType errorType) {
        ErrorResponse response = new ErrorResponse(errorType.getErrorCode(),message);
        return getResponseBuilder((S)response).copyHeaders(getCORSHeaders()).setHeader("statuscode", errorType.getStatusCode()).build();
    }
}
