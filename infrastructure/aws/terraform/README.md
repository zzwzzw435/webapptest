# CSYE 6225 - Spring 2019

## Technology Stack
- OS- Ubuntu 18.04(Linux)
- Programming Language - HCL
- tools -  Terraform 0.12.10
- platform - aws 

## Build Instructions
- install terraform 0.12.10
- go to path where .tf file in(../terraform)
- To apply run $terraform apply -var-file="dev_variables.tfvars"(if want to use prod then change to "prod_variables.tfvars")


## Deploy Instructions
- To destroy run $terraform destroy -var-file="dev_variables.tfvars"
