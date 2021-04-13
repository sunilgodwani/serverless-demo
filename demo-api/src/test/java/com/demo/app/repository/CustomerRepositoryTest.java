package com.demo.app.repository;

import com.demo.app.IntegrationTestBase;
import com.demo.app.entity.CustomerEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {IntegrationTestBase.IntegrationTestConfig.class})
public class CustomerRepositoryTest extends IntegrationTestBase {

    Logger LOG = LoggerFactory.getLogger(CustomerRepositoryTest.class);

    @Autowired
    private CustomerRespository customerRespository;

    @Test
    public void testCRUD() {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setEmailAddress("test@demo.com");
        customerRespository.save(customerEntity);

        CustomerEntity customerEntity2 = new CustomerEntity();
        customerEntity2.setEmailAddress("test@demo.com");
        customerRespository.save(customerEntity2);

        /*CustomerEntity response =  customerRespository.findByEmailAddress("test@demo.com");
        Assert.assertNotNull(response);

        customerRespository.delete(response);
        response =  customerRespository.findByEmailAddress("test@demo.com");
        Assert.assertNull(response);*/
    }
}
