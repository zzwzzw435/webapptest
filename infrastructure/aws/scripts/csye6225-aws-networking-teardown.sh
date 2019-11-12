#!/bin/bash
#Usage: Deleting networking resources such as Virtual Private Cloud (VPC), Internet Gateway, Route Table and Routes with a arugment STACK_NAME

echo "dev/prod"
read aws_profile
if [ -z $aws_profile ]; then
    echo "You need to choose a proper profile"
    exit 1
fi

export AWS_PROFILE=$aws_profile
echo "enter the name of vpc you want to delete:"
read vpcName
if [ -z $vpcName ]; then
    echo "You need to provide a vpcName"
    exit 1
fi

#Get a vpc-Id using the name provided
vpcId=`aws ec2 describe-vpcs --filter "Name=tag:Name,Values=$vpcName" --query 'Vpcs[*].{id:VpcId}' --output text`
if [ $? -ne 0 ]; then
    exit 1
fi
#Get a Internet Gateway Id using the name provided
gatewayId=`aws ec2 describe-internet-gateways --filter "Name=tag:Name,Values="$vpcName"_csye6225-InternetGateway" --query 'InternetGateways[*].{id:InternetGatewayId}' --output text`
if [ $? -ne 0 ]; then
    exit 1
fi
#Get a route table Id using the name provided
routeTableId=`aws ec2 describe-route-tables --filter "Name=tag:Name,Values="$vpcName"_csye6225-public-route-table" --query 'RouteTables[*].{id:RouteTableId}' --output text`
if [ $? -ne 0 ]; then
    exit 1
fi
subnetId1=`aws ec2 describe-subnets --filter "Name=tag:Name,Values="$vpcName"_csyeSubnet1" --query 'Subnets[0].SubnetId' | tr -d '""' `
if [ $? -ne 0 ]; then
    exit 1
fi
echo "subnet1: $subnetId1"
subnetId2=`aws ec2 describe-subnets --filter "Name=tag:Name,Values="$vpcName"_csyeSubnet2" --query 'Subnets[0].SubnetId' | tr -d '""' `
if [ $? -ne 0 ]; then
    exit 1
fi
echo "subnet2: $subnetId2"
subnetId3=`aws ec2 describe-subnets --filter "Name=tag:Name,Values="$vpcName"_csyeSubnet3" --query 'Subnets[0].SubnetId' | tr -d '""' `
if [ $? -ne 0 ]; then
    exit 1
fi
echo "subnet3: $subnetId3"

#delete subnet
aws ec2 delete-subnet --subnet-id $subnetId1
if [ $? -ne 0 ]; then
    exit 1
fi
aws ec2 delete-subnet --subnet-id $subnetId2
if [ $? -ne 0 ]; then
    exit 1
fi
aws ec2 delete-subnet --subnet-id $subnetId3
if [ $? -ne 0 ]; then
    exit 1
fi

#Delete the route
echo "Deleting route..."
aws ec2 delete-route --route-table-id $routeTableId --destination-cidr-block 0.0.0.0/0
if [ $? -ne 0 ]; then
    exit 1
fi
echo "Deleting route... Success"

#Delete the route table
echo "Deleting route table $routeTableId..."
aws ec2 delete-route-table --route-table-id $routeTableId
if [ $? -ne 0 ]; then
    exit 1
fi
echo "Deleting route table $routeTableId... Success"

#Detach Internet gateway and vpc
echo "Detaching Internet Gateway from vpc..."
aws ec2 detach-internet-gateway --internet-gateway-id $gatewayId --vpc-id $vpcId
if [ $? -ne 0 ]; then
    exit 1
fi
echo "Detaching Internet Gateway from vpc... Success"

#Delete the Internet gateway
echo "Deleting Internet gateway $gatewayId"
aws ec2 delete-internet-gateway --internet-gateway-id $gatewayId
if [ $? -ne 0 ]; then
    exit 1
fi
echo "Deleting Internet gateway $gatewayId Success"

#Delete the vpc
echo "Deleting the VPC $vpcId"
aws ec2 delete-vpc --vpc-id $vpcId
if [ $? -ne 0 ]; then
    exit 1
fi
echo "Deleting the VPC $vpcId Success"

echo "All requireds network resourses Deleted"