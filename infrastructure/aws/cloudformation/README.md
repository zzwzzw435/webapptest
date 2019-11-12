# CSYE6225-fall2019
Programming Infrastructure Using AWS CloudFormation

pre-requirement:
1. Build csye6225-cf-networking.yaml file under the same director of shell script. You can either choose yaml or json for Cloudformation tamplate
2. Build credentials and config files under ~/.aws director for aws cli configuration

required input variables:
 * profile name
 * VPC CIDR (or use default when nothing input)
 * subnets CIDR (or use default when nothing input)

Steps:
1. Run ./csye6225-aws-cf-create-stack.sh for creating stacks for networking resources Using AWS Cloudformation. Follow the console output instruction to provide proper run-time variables.
2. Run ./csye6225-aws-cf-terminate-stack.sh for deleting stacks using AWS Cloudformation. Follow the console output instruction to provide proper run-time variables