
################################################################################################################
###################################### Cloudformation Execution Role ############################################
#PARAM_FILE="demo-cdk/config/cdk-stack-param.json"
#CFN_EXECUTION_ROLE=$(cat $PARAM_FILE | jq -r '.CFN_EXECUTION_ROLE')
#echo $CFN_EXECUTION_ROLE
##################################################################################################################
############################################## CDK Bootstrap & Deploy ############################################
mvn clean install package
cdk bootstrap
cdk synth
cdk diff
cdk deploy demo-serverless --require-approval never
###################################################################################################################