#!/bin/bash
# Variables
dVpcCIDR="10.192.0.0/16"
dPublicSubnet1CIDR="10.192.0.0/24"
dPublicSubnet2CIDR="10.192.16.0/24"
dPublicSubnet3CIDR="10.192.32.0/24"

param1=""
param2=""
param3=""
param3=""

echo "Please input stack-name"
echo -n "stack-name: "
read "stackname"
if [ -z $stackname ]; then
    echo "you must provide a stackname"
    exit 1
fi

echo "Please input profile you choose (dev | prod)"
echo -n "profile: "
read "profile"
if [ -z $profile ]; then
    echo "you must choose a profile"
    exit 1
fi

echo "Please input vpc name"
echo -n "vpc name: "
read "vpcname"
if [ -z $vpcname ]; then
    echo "you must provide a vpc name"
    exit 1
fi

echo "Please input vpc CIDR: "
echo -n "vpc-CIDR: "
read "VpcCIDR"
if [ -z $VpcCIDR ]; then
    echo "Use default vpc cidr: $dVpcCIDR"
else
    param1=" ParameterKey=VpcCIDR,ParameterValue=$VpcCIDR"
fi

echo "Please input subnet1 CIDR: "
echo -n "subnet1-CIDR: "
read "PublicSubnet1CIDR"
if [ -z $PublicSubnet1CIDR ]; then
    echo "Use default subnet1 cidr: $dPublicSubnet1CIDR"
else
    param2=" ParameterKey=PublicSubnet1CIDR,ParameterValue=$PublicSubnet1CIDR"
fi

echo "Please input subnet2 CIDR: "
echo -n "subnet2-CIDR: "
read "PublicSubnet2CIDR"
if [ -z $PublicSubnet2CIDR ]; then
    echo "Use default subnet2 cidr: $dPublicSubnet2CIDR"
else
    param3=" ParameterKey=PublicSubnet2CIDR,ParameterValue=$PublicSubnet2CIDR"
fi

echo "Please input subnet3 CIDR: "
echo -n "subnet3-CIDR: "
read "PublicSubnet3CIDR"
if [ -z $PublicSubnet3CIDR ]; then
    echo "Use default subnet3 cidr: $dPublicSubnet3CIDR"
else
    param4=" ParameterKey=PublicSubnet3CIDR,ParameterValue=$PublicSubnet3CIDR"
fi
    

echo "creating cloudformation stack for stack-name: $stackname"
aws cloudformation create-stack --stack-name $stackname --template-body file://csye6225-cf-networking.yaml --parameters ParameterKey=EnvironmentName,ParameterValue=$vpcname$param1$param2$param3$param4 --profile $profile

if [ $? -ne 0 ]; then
    exit 1
fi

echo "waiting cloudformation create complete"
aws cloudformation wait stack-create-complete --stack-name $stackname --profile $profile

if [ $? -eq 0 ]; then
    echo "STACK CREATION COMPLETE"
else
    echo 'Error occurred.Dont proceed. TERMINATED'
    echo "$?"
    exit 1
fi