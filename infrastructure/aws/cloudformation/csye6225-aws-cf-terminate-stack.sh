#!/bin/bash

#Variables
echo "Please input stack name: "
echo -n "stackname: "
read "stackname"
if [ -z $stackname ]; then
    echo "you must provide a stack name"
    exit 1
fi
echo "Please input the profile name (dev | prod):"
echo -n "profile name: "
read "profile"
if [ -z $profile ]; then
    echo "you must choose a profile"
    exit 1
fi

echo "deleting below stack"
aws cloudformation describe-stacks --stack-name $stackname --profile $profile
if [ $? -ne 0 ]; then
    exit 1
fi

echo "delete cloudformation stack name: $stackname"
stack=$(aws cloudformation delete-stack --stack-name $stackname --profile $profile)
echo "Waiting the cloudformation to finish the deletion"
aws cloudformation wait stack-delete-complete --stack-name $stackname --profile $profile

if [ $? -eq 0 ]; then
    echo "delete stack $stackname success..."
else
    echo "deletion falied"
    exit 1
fi