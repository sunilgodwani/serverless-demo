# serverless-demo



Set AWS profile to targeted account profile

export AWS_PROFILE=<admin profile of taregted account>

Verify target account by running
aws sts get-caller-identity

bootstrap targeted environment as admin if not already bootstraped

cdk bootstrap

Run ./deploy.sh



