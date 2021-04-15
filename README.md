# serverless-demo


NOTE: In this demo, dynamodb table customers with partition key 'customer_id' needs to created first. DynamoDB resources are not created as part of this CDK stack. It only adds permission for existing resources.

Configure admin profile of target AWS account where you would like to deploy this stack.
Set AWS profile to targeted account profile

export AWS_PROFILE=admin profile name

Verify target account by running
aws sts get-caller-identity

Run ./deploy.sh



