package com.demo.app.repository;

import com.demo.app.entity.CustomerEntity;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface CustomerRespository extends CrudRepository<CustomerEntity, String> {

    CustomerEntity findByEmailAddress(String emailAddress);
}
