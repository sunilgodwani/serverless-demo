package com.demo.app.config;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSCognitoIdentityProviderClientConfig {

    @Bean
    public AWSCognitoIdentityProvider awsCognitoIdentityProvider() {
        return AWSCognitoIdentityProviderClientBuilder.standard().withRegion(Regions.fromName(System.getenv("AWS_DEFAULT_REGION"))).build();
    }

}
