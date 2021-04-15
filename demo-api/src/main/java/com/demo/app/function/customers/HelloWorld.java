package com.demo.app.function.customers;

import com.demo.app.annotation.LambdaFunctionConfig;
import com.demo.app.entity.CustomerEntity;
import com.demo.app.function.APIGatewayFunction;
import com.demo.app.repository.CustomerRespository;
import com.demo.app.request.FunctionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import com.demo.app.response.*;


@Component("helloWorld")
@LambdaFunctionConfig(name="helloWorld", httpMethod = "GET", apiGatewayPath = "hello")
public class HelloWorld extends APIGatewayFunction<FunctionRequest, FunctionResponse> {

    private Logger LOG = LoggerFactory.getLogger(GetCustomers.class);

    @Autowired
    private CustomerRespository customerRespository;

    @Override
    protected FunctionResponse functionHandler(Message<FunctionRequest> voidMessage) {
        LOG.info(appendContext("Processing GetEstimates request"));
        return new EmptyResponse("Hello from lambda");
    }
}
