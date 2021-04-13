package com.demo.app.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LambdaFunctionConfig {
    public String name();
    public String httpMethod();
    public String[] apiGatewayPath();
    public String pathParameter() default "";
}
