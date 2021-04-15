package com.demo.app.cdk;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.codedeploy.LambdaDeploymentConfig;
import software.amazon.awscdk.services.codedeploy.LambdaDeploymentGroup;
import software.amazon.awscdk.services.cognito.IUserPool;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.dynamodb.ITable;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.events.IRule;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.RuleProps;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.*;

import java.util.*;
import java.util.stream.Collectors;

public class DemoServerlessStack extends Stack {

    private static String DEFAULT_REGION = "ap-southeast-2";

    public DemoServerlessStack(final Construct parent, final String name, @Nullable StackProps props) {

        super(parent, name, props);

        RestApi api = getRestAPI();

        Map<String, IResource> apiGatewayResources = new HashMap<>();
        List<String> corsPaths = new ArrayList<>();

        //IUserPool userPool = UserPool.fromUserPoolArn(this, "user-pool", "arn:aws:cognito-idp:ap-southeast-2:094355023285:userpool/ap-southeast-2_UJOVGvNxy");

        /*Authorizer authorizer = new CognitoUserPoolsAuthorizer(this, "api-gateway-authoriser", CognitoUserPoolsAuthorizerProps.builder()
                .authorizerName("api-gateway-authoriser")
                .identitySource("method.request.header.Authorization")
                .cognitoUserPools(Arrays.asList(userPool))
                .build());*/

        List<ITable> tables  = getDynamodbTables();

        List<LambdaFunction> targetlambdaFunctions = new ArrayList<>();

        CDKUtils.getFunctions().stream().forEach(lambdaFunctionDefinition -> {
            Function function = createLambdaFunction(lambdaFunctionDefinition.name());
            createAPIIntegration(apiGatewayResources, corsPaths, null, api,function,lambdaFunctionDefinition.httpMethod(), lambdaFunctionDefinition.apiGatewayPath(), lambdaFunctionDefinition.pathParameter());
            addDynamoDBTablePermissions(function, tables);
            targetlambdaFunctions.add(new LambdaFunction(function));
        });

        //createDynamoDBAPIIntegration(apiGatewayResources, null, api, "GET", "job-types", null);

        addCloudwatchEvent(targetlambdaFunctions);
    }

    private void addCloudwatchEvent(List<LambdaFunction> targetlambdaFunctions) {

        int ruleCount = 1;
        Map<String, List<LambdaFunction>> targetMap = new HashMap<>();
        List<LambdaFunction> functionList = null;
        int totalFunctions = targetlambdaFunctions.size();
        for(int i=1; i <= totalFunctions; i++) {
            if(i%5 == 1) {
                functionList = new ArrayList<>();
                functionList.add(targetlambdaFunctions.get(i-1));
            } else {
                functionList.add(targetlambdaFunctions.get(i-1));
            }

            if(i%5 == 0 || i == totalFunctions) {
                targetMap.put("lambda-demo-warm-up-rule-"+ruleCount, functionList);
                ruleCount++;
            }
        }

        targetMap.keySet().stream().forEach(ruleName -> {
            IRule eventRule  = new Rule(this, ruleName, RuleProps.builder().ruleName(ruleName)
                    .schedule(Schedule.rate(Duration.minutes(1)))
                    .targets(targetMap.get(ruleName)).build());
        });

    }

    private Function createLambdaFunction(String functionName) {

        Map<String, String> lambdaEnvMap = new HashMap<>();
        lambdaEnvMap.put("FUNCTION_NAME", functionName);

        Function function = new Function(this, functionName, getLambdaFunctionProps(lambdaEnvMap));
        addDeploymentGroup(function, functionName);


        return function;
    }

    private void createDynamoDBAPIIntegration(Map<String, IResource> apiGatewayResources, Authorizer authorizer, RestApi api, String httpMethod, String apiGatewayPath, String pathParameter) {
        IResource mainResource = null;
        IResource integrationResource = null;
        Integration awsIntegration = null;

        if(apiGatewayResources.get(apiGatewayPath) != null) {
            mainResource = apiGatewayResources.get(apiGatewayPath);
        } else {
            mainResource = api.getRoot().addResource(apiGatewayPath);
            apiGatewayResources.put(apiGatewayPath, mainResource);
        }

        if(StringUtils.hasLength(pathParameter)) {
            if(mainResource.getResource("{"+pathParameter+"}") != null) {
                integrationResource = mainResource.getResource("{"+pathParameter+"}");
            } else {
                integrationResource = mainResource.addResource("{"+pathParameter+"}");
            }
        } else {
            integrationResource = mainResource;

        }

        List<IntegrationResponse> integrationResponses = new ArrayList<>();
        Map<String, String> requestTemplate = new HashMap<>();
        requestTemplate.put("application/json", "{\n" +
                "    \"TableName\": \"job_type\"\n" +
                "}");

        Map<String, String> responseTemplate = new HashMap<>();
        responseTemplate.put("application/json","#set($inputRoot = $input.path('$'))\n" +
                "{ \"job_types\" : [\n" +
                "        #foreach($item in $inputRoot.Items) {\n" +
                "            \"job_type\": \"$item.job_type.S\",\n" +
                "            \"categories\":[\n" +
                "            #foreach($category in $item.categories.L) {\n" +
                "                \"category_name\": \"$category.M.category_name.S\",\n" +
                "                \"subcategories\":[\n" +
                "                 #foreach($subcategory in $category.M.subcategories.L) {\n" +
                "                    \"subcategory_name\": \"$subcategory.M.subcategory_name.S\"\n" +
                "                    \n" +
                "                }#if($foreach.hasNext),#end\n" +
                "        \t    #end\n" +
                "                ]\n" +
                "                \n" +
                "            }#if($foreach.hasNext),#end\n" +
                "    \t    #end\n" +
                "            ]\n" +
                "        }#if($foreach.hasNext),#end\n" +
                "\t#end\n" +
                "    ]\n" +
                "}");

        Map<String, String> integrationResponseParameters = new HashMap<>();
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Headers","'Content-Type,X-Amz-Date,access_token,Authorization,X-Api-Key,X-Amz-Security-Token,X-Amz-User-Agent'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Origin","'*'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Credentials","'false'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Methods","'OPTIONS,GET,PUT,POST,DELETE'");

        integrationResponses.add(IntegrationResponse.builder()
                .contentHandling(ContentHandling.CONVERT_TO_TEXT)
                .responseParameters(integrationResponseParameters)
                .responseTemplates(responseTemplate)
                .statusCode("200")
                .build());

        IRole apiGatewayExecutionRole = Role.fromRoleArn(this, "apiGatewayExecutionRole", "arn:aws:iam::094355023285:role/api-gateway-execution-role");
        awsIntegration = new AwsIntegration(AwsIntegrationProps.builder()
                .service("dynamodb")
                .region(DEFAULT_REGION)
                .integrationHttpMethod("POST")
                .action("Scan")
                .options(IntegrationOptions.builder()
                        .credentialsRole(apiGatewayExecutionRole)
                        .contentHandling(ContentHandling.CONVERT_TO_TEXT)
                        .requestTemplates(requestTemplate)
                        .integrationResponses(integrationResponses)
                        .build())
                .build());


        List<MethodResponse> methodResponses = new ArrayList<>();

        Map<String, Boolean> responseParameters = new HashMap<>();
        responseParameters.put("method.response.header.Access-Control-Allow-Headers", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Methods", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Credentials", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Origin", Boolean.TRUE);

        Map<String, IModel> responseModel = new HashMap<>();
        responseModel.put("application/json", new EmptyModel());
        methodResponses.add(MethodResponse.builder()
                .responseModels(responseModel)
                .responseParameters(responseParameters)
                .statusCode("200")
                .build());


        if(authorizer != null) {
            integrationResource.addMethod(httpMethod, awsIntegration, MethodOptions.builder().methodResponses(methodResponses).authorizationType(AuthorizationType.COGNITO).authorizer(authorizer).build());
        } else {
            integrationResource.addMethod(httpMethod, awsIntegration, MethodOptions.builder().methodResponses(methodResponses).build());
        }
        addCorsOptions(integrationResource);

    }

    private void createAPIIntegration(Map<String, IResource> apiGatewayResources, List<String> corsPaths, Authorizer authorizer, RestApi api, Function function, String httpMethod, String[] apiGatewayPath, String pathParameter) {
        IResource mainResource = null;
        IResource integrationResource = null;
        Integration functionIntegration = null;

        IResource prevResource = null;
        String integrationPath = "";

        for (String path: apiGatewayPath) {
            if(apiGatewayResources.get(path) != null) {
                mainResource = apiGatewayResources.get(path);
            } else {
                if (prevResource == null) {
                    prevResource = api.getRoot();
                }
                mainResource = prevResource.addResource(path);
                apiGatewayResources.put(path, mainResource);
            }
            integrationPath = integrationPath +"/"+ path;
            prevResource = mainResource;
        }



        if(StringUtils.hasLength(pathParameter)) {
            if(mainResource.getResource("{"+pathParameter+"}") != null) {
                integrationResource = mainResource.getResource("{"+pathParameter+"}");
            } else {
                integrationResource = mainResource.addResource("{"+pathParameter+"}");
            }
            integrationPath = integrationPath+"/"+"{"+pathParameter+"}";
        } else {
            integrationResource = mainResource;
        }

        functionIntegration = new LambdaIntegration(function);


        if(authorizer != null) {
            integrationResource.addMethod(httpMethod, functionIntegration, MethodOptions.builder().authorizationType(AuthorizationType.COGNITO).authorizer(authorizer).build());
        } else {
            integrationResource.addMethod(httpMethod, functionIntegration);
        }

        if(!corsPaths.contains(integrationPath)) {
            addCorsOptions(integrationResource);
            corsPaths.add(integrationPath);
        }


    }

    private RestApi getRestAPI() {

        RestApi api = new RestApi(this, "serverless-demo-api", RestApiProps.builder().restApiName("serverless-demo-api").build());

        Map<String, String> gatewayResponseHeaders = new HashMap<>();
        gatewayResponseHeaders.put("gatewayresponse.header.Access-Control-Allow-Headers","'Content-Type,X-Amz-Date,access_token,Authorization,X-Api-Key,X-Amz-Security-Token,X-Amz-User-Agent'");
        gatewayResponseHeaders.put("gatewayresponse.header.Access-Control-Allow-Origin","'*'");
        gatewayResponseHeaders.put("gatewayresponse.header.Access-Control-Allow-Credentials","'false'");
        gatewayResponseHeaders.put("gatewayresponse.header.Access-Control-Allow-Methods","'OPTIONS,GET,PUT,POST,DELETE'");

        api.addGatewayResponse("4XX", GatewayResponseOptions.builder()
                .type(ResponseType.DEFAULT_4_XX)
                .responseHeaders(gatewayResponseHeaders)
                .build());

        api.addGatewayResponse("5XX", GatewayResponseOptions.builder()
                .type(ResponseType.DEFAULT_5_XX)
                .responseHeaders(gatewayResponseHeaders)
                .build());
        return api;
    }

    private void addDynamoDBTablePermissions(Function function, List<ITable> tables) {
        tables.stream().forEach(iTable -> {
            iTable.grantReadWriteData(function);
        });
    }

    private List<ITable> getDynamodbTables() {
        return CDKUtils.getTableNames()
                .stream()
                .map(tableName -> Table.fromTableName(this, tableName, tableName))
                .collect(Collectors.toList());
    }


    private void addDeploymentGroup(Function function, String functionName) {
        Version version = function.getCurrentVersion();
        Alias functionAlias = Alias.Builder.create(this, functionName+"Alias")
                .aliasName(functionName+"Alias")
                .version(version).build();

        LambdaDeploymentGroup.Builder.create(this, functionName+"DeploymentGroup")
                .alias(functionAlias)
                .deploymentConfig(LambdaDeploymentConfig.ALL_AT_ONCE).build();
    }

    private List<IntegrationResponse> getIntegrationResponses() {
        List<IntegrationResponse> integrationResponses = new ArrayList<>();

        Map<String, String> integrationResponseParameters = new HashMap<>();
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Headers","'Content-Type,X-Amz-Date,access_token,Authorization,X-Api-Key,X-Amz-Security-Token,X-Amz-User-Agent'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Origin","'*'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Credentials","'false'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Methods","'OPTIONS,GET,PUT,POST,DELETE'");
        integrationResponses.add(IntegrationResponse.builder()
                .responseParameters(integrationResponseParameters)
                .statusCode("200")
                .build());

        return integrationResponses;
    }

    private void addCorsOptions(IResource item) {
        List<MethodResponse> methoedResponses = new ArrayList<>();

        Map<String, Boolean> responseParameters = new HashMap<>();
        responseParameters.put("method.response.header.Access-Control-Allow-Headers", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Methods", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Credentials", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Origin", Boolean.TRUE);

        Map<String, IModel> responseModel = new HashMap<>();
        responseModel.put("application/json", new EmptyModel());
        methoedResponses.add(MethodResponse.builder()
                .responseModels(responseModel)
                .responseParameters(responseParameters)
                .statusCode("200")
                .build());
        MethodOptions methodOptions = MethodOptions.builder()
                .methodResponses(methoedResponses)
                .build()
                ;

        Map<String, String> requestTemplate = new HashMap<>();
        requestTemplate.put("application/json","{\"statusCode\": 200}");

        Integration methodIntegration = MockIntegration.Builder.create()
                .integrationResponses(getIntegrationResponses())
                .passthroughBehavior(PassthroughBehavior.NEVER)
                .requestTemplates(requestTemplate)
                .build();

        item.addMethod("OPTIONS", methodIntegration, methodOptions);
    }

    private FunctionProps getLambdaFunctionProps(Map<String, String> lambdaEnvMap) {

        lambdaEnvMap.put("MAIN_CLASS", "com.demo.app.FunctionConfiguration");

        return FunctionProps.builder()
                .code(Code.fromAsset("demo-api/target/demo-api-1.0.0-aws.jar"))
                .handler("org.springframework.cloud.function.adapter.aws.SpringBootApiGatewayRequestHandler")
                .runtime(Runtime.JAVA_8)
                .environment(lambdaEnvMap)
                .timeout(Duration.seconds(60))
                .memorySize(512)
                .build();
    }


}
