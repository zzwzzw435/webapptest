#!/bin/bash
#variable used in scripts:

#vpcName="csye6225-vpc"
subnetName1="csyeSubnet1"
subnetName2="csyeSubnet2"
subnetName3="csyeSubnet3"
gatewayName="csye6225-InternetGateway"
routeTableName="csye6225-public-route-table"
dvpcCidrBlock="192.168.0.0/16"
dsubNetCidrBlock1="192.168.0.0/20"
dsubNetCidrBlock2="192.168.16.0/20"
dsubNetCidrBlock3="192.168.32.0/20"
destinationCidrBlock="0.0.0.0/0"

#create VPC
echo "dev/prod"
read aws_profile
if [ -z $aws_profile ]; then
  echo "you should choose a profile"
  exit 1
fi

echo "enter region(1/2)"
read num
if [ -z $num ]; then
  echo "you should choose a region"
  exit 1
fi

echo "creating VPC... now"
echo "enter VPC name"
read vpcName
if [ -z $vpcName ]; then
  echo "you should enter a vpcName"
  exit 1
fi

echo "enter vpcCidrBlock"
read vpcCidrBlock
if [ -z $vpcCidrBlock ]; then
  vpcCidrBlock="$dvpcCidrBlock"
  echo "No CIDR block input use default $vpcCidrBlock"
fi

echo "enter subnet1 cidr Block"
read subNetCidrBlock1
if [ -z $subNetCidrBlock1 ]; then
  subNetCidrBlock1="$dsubNetCidrBlock1"
  echo "No subnet 1 CIDR block input use default $subNetCidrBlock1"
  
fi

echo "enter subnet2 cidr Block"
read subNetCidrBlock2
if [ -z $subNetCidrBlock2 ]; then
  subNetCidrBlock2="$dsubNetCidrBlock2"
  echo "No subnet 2 CIDR block input use default $subNetCidrBlock2"
  
fi

echo "enter subnet2 cidr Block"
read subNetCidrBlock3
if [ -z $subNetCidrBlock3 ]; then
  subNetCidrBlock3="$dsubNetCidrBlock3"
  echo "No subnet 3 CIDR block input use default $subNetCidrBlock3"
  
fi


availabilityZone1="us-east-"$num"a"
availabilityZone2="us-east-"$num"b"
availabilityZone3="us-east-"$num"c"

export AWS_PROFILE=$aws_profile
VPC_ID=`aws ec2 create-vpc --cidr-block "$vpcCidrBlock" --query 'Vpc.VpcId' --output text`
if [ $? -ne 0 ]; then
    exit 1
fi
aws ec2 create-tags --resources $VPC_ID --tags Key=Name,Value=$vpcName 
if [ $? -ne 0 ]; then
    exit 1
fi
echo "VPC:csye6225-vpc Id:$VPC_ID created successfully"

#create InternetGateway
echo "creating Internet Gateway..."
InternetGatewayId=`aws ec2 create-internet-gateway --query 'InternetGateway.InternetGatewayId' --output text`
if [ $? -ne 0 ]; then
    exit 1
fi
aws ec2 create-tags --resources $InternetGatewayId --tags Key=Name,Value=$vpcName"_"$gatewayName
if [ $? -ne 0 ]; then
    exit 1
fi
echo "InternetGateway:csye6225-InternetGateway ID:$InternetGatewayId created successfully"

#attatch InternetGateway to VPC
echo "attaching InternetGateway to VPC..."
aws ec2 attach-internet-gateway --internet-gateway-id $InternetGatewayId --vpc-id $VPC_ID
if [ $? -ne 0 ]; then
    exit 1
fi
echo "attaching successfully"

# Create Public Subnet
echo "Creating Public Subnet..."
SUBNET_PUBLIC_ID1=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block $subNetCidrBlock1 \
  --availability-zone $availabilityZone1 \
  --query 'Subnet.{SubnetId:SubnetId}' \
  --output text)
if [ $? -ne 0 ]; then
    exit 1
fi
aws ec2 create-tags --resources $SUBNET_PUBLIC_ID1 --tags Key=Name,Value=$vpcName"_"$subnetName1
if [ $? -ne 0 ]; then
    exit 1
fi

echo "  Subnet ID '$SUBNET_PUBLIC_ID1' CREATED in '$availabilityZone1'" \
  "Availability Zone."
SUBNET_PUBLIC_ID2=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block $subNetCidrBlock2 \
  --availability-zone $availabilityZone2 \
  --query 'Subnet.{SubnetId:SubnetId}' \
  --output text)
if [ $? -ne 0 ]; then
    exit 1
fi
aws ec2 create-tags --resources $SUBNET_PUBLIC_ID2 --tags Key=Name,Value=$vpcName"_"$subnetName2
if [ $? -ne 0 ]; then
    exit 1
fi
echo "  Subnet ID '$SUBNET_PUBLIC_ID2' CREATED in '$availabilityZone2'" \
  "Availability Zone."
SUBNET_PUBLIC_ID3=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block $subNetCidrBlock3 \
  --availability-zone $availabilityZone3 \
  --query 'Subnet.{SubnetId:SubnetId}' \
  --output text)
if [ $? -ne 0 ]; then
    exit 1
fi
aws ec2 create-tags --resources $SUBNET_PUBLIC_ID3 --tags Key=Name,Value=$vpcName"_"$subnetName3
if [ $? -ne 0 ]; then
    exit 1
fi
echo "  Subnet ID '$SUBNET_PUBLIC_ID3' CREATED in '$availabilityZone3'" \
  "Availability Zone."

#create RouteTable
echo "creating Route Table..."
RouteTableId=`aws ec2 create-route-table --vpc-id $VPC_ID --query 'RouteTable.RouteTableId' --output text`
if [ $? -ne 0 ]; then
    exit 1
fi
aws ec2 create-tags --resources $RouteTableId --tags Key=Name,Value=$vpcName"_"$routeTableName
if [ $? -ne 0 ]; then
    exit 1
fi
echo "RouteTable:csye6225-public-route-table ID:$RouteTableId created successfully"

RouteStatus=`aws ec2 create-route --route-table-id $RouteTableId --destination-cidr-block $destinationCidrBlock --gateway-id $InternetGatewayId --query 'Return'`
if [ $? -ne 0 ]; then
    exit 1
fi
echo "Route created in RouteTable $RouteTableId with a target to Gateway $InternetGatewayId status: $RouteStatus"

aws ec2 associate-route-table --subnet-id "$SUBNET_PUBLIC_ID1" --route-table-id "$RouteTableId"
if [ $? -ne 0 ]; then
    exit 1
fi
aws ec2 associate-route-table --subnet-id "$SUBNET_PUBLIC_ID2" --route-table-id "$RouteTableId"
if [ $? -ne 0 ]; then
    exit 1
fi
aws ec2 associate-route-table --subnet-id "$SUBNET_PUBLIC_ID3" --route-table-id "$RouteTableId"
if [ $? -ne 0 ]; then
    exit 1
fi
echo "All requireds network resourses created"