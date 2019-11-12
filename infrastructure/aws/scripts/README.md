# CSYE6225-fall2019
Programming Infrastructure Using AWS cli commandline interface

pre-requirement:

1. Build credentials and config files under ~/.aws director for aws cli configuration

required input variables:
 * profile name
 * zone
 * VPC CIDR (or use default when nothing input)
 * subnets CIDR (or use default when nothing input)

Steps:

1. Run csye6225-aws-networking-setup.sh for creating networking resources  Using AWS cli commandline interface. Follow the console output instruction to provide proper run-time variables.
2. Run csye6225-aws-networking-teardown.sh for deleting networking resources  Using AWS cli commandline interface. Follow the console output instruction to provide proper run-time variables