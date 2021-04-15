# serverless-demo


NOTE: In this demo, dynamodb table customers with partition key 'customer_id' needs to created first. DynamoDB resources are not created as part of this CDK stack. It only adds permission for existing resources.
Set AWS profile to targeted account profile

export AWS_PROFILE=<admin profile of taregted account>

Verify target account by running
aws sts get-caller-identity

bootstrap targeted environment as admin if not already bootstraped

cdk bootstrap

Run ./deploy.sh



