package com.demo.app.cdk;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.StackProps;

public class DemoServerlessApp {

    public static void main(final String[] args) {
        App app = new App();
        String stackName = "demo-serverless";
        new DemoServerlessStack(app, stackName, StackProps.builder().build());
        app.synth();
    }
}
