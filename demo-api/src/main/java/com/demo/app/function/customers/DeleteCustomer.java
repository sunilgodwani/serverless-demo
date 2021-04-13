package com.demo.app.function.customers;

import com.demo.app.repository.CustomerRespository;
import com.demo.app.request.FunctionRequest;
import com.demo.app.annotation.LambdaFunctionConfig;
import com.demo.app.function.APIGatewayFunction;
import com.demo.app.response.EmptyResponse;
import com.demo.app.response.FunctionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component("deleteCustomer")
@LambdaFunctionConfig(name="deleteCustomer", httpMethod = "DELETE", apiGatewayPath = "customer", pathParameter = "customerId")
public class DeleteCustomer extends APIGatewayFunction<FunctionRequest, FunctionResponse> {

    private Logger LOG = LoggerFactory.getLogger(DeleteCustomer.class);

    @Autowired
    private CustomerRespository customerRespository;

    @Override
    protected FunctionResponse functionHandler(Message<FunctionRequest> voidMessage) {
        LOG.info(appendContext("Processing DeleteCustomer request"));
        customerRespository.delete(customerRespository.findById(voidMessage.getHeaders().get("customerId").toString()).get());
        return new EmptyResponse();
    }
}
