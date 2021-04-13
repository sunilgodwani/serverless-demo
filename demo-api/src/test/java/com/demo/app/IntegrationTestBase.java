package com.demo.app;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


public class IntegrationTestBase {

    @Configuration
    @EnableDynamoDBRepositories(basePackages = "com.serverless.demo.app.repository")
    public static class IntegrationTestConfig {

        @Value("${amazon.aws.role.arn}")
        private String amazonAWSRoleArn;

        public AWSCredentialsProvider amazonAWSCredentialsProvider() {
            return new AWSStaticCredentialsProvider(amazonAWSCredentials());
        }

        public AWSCredentials amazonAWSCredentials() {

            AWSSecurityTokenService client = AWSSecurityTokenServiceClientBuilder.standard()
                    .withRegion(Regions.AP_SOUTHEAST_2)
                    .build();

            AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest()
                    .withRoleArn(amazonAWSRoleArn)
                    .withRoleSessionName("integration-test-session")
                    .withDurationSeconds(900);


            AssumeRoleResult roleResponse = client.assumeRole(assumeRoleRequest);
            Credentials sessionCredentials = roleResponse.getCredentials();

            BasicSessionCredentials awsSessionCredentials = new BasicSessionCredentials(
                    sessionCredentials.getAccessKeyId(),
                    sessionCredentials.getSecretAccessKey(),
                    sessionCredentials.getSessionToken());

            return awsSessionCredentials;
        }

        @Bean
        public AmazonDynamoDB amazonDynamoDB() {
            return AmazonDynamoDBClientBuilder.standard().withCredentials(amazonAWSCredentialsProvider())
                    .withRegion(Regions.AP_SOUTHEAST_2).build();
        }

    }

}
