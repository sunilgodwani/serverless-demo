package com.demo.app.function.customers;

import com.demo.app.annotation.LambdaFunctionConfig;
import com.demo.app.entity.CustomerEntity;
import com.demo.app.function.APIGatewayFunction;
import com.demo.app.repository.CustomerRespository;
import com.demo.app.request.CustomerRequest;
import com.demo.app.response.EmptyResponse;
import com.demo.app.response.FunctionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component("createCustomer")
@LambdaFunctionConfig(name="createCustomer", httpMethod = "POST", apiGatewayPath = "customer")
public class CreateCustomer extends APIGatewayFunction<CustomerRequest, FunctionResponse> {

    private Logger LOG = LoggerFactory.getLogger(GetCustomers.class);

    @Autowired
    private CustomerRespository estimateRepository;

    @Override
    protected FunctionResponse functionHandler(Message<CustomerRequest> requestMessage) {
        LOG.info(appendContext("Processing createCustomer request"));

        CustomerEntity customer = new CustomerEntity();
        customer.setEmailAddress(requestMessage.getPayload().getEmailAddress());
        estimateRepository.save(customer);

        return new EmptyResponse(customer.getCustomerId());
    }
}
