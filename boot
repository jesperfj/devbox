#!/bin/bash

STACK_NAME=devbox
IMAGE_NAME=$1
INSTANCE_NAME=$2
INSTANCE_TYPE=$3

if [[ -z $IMAGE_NAME || -z $INSTANCE_NAME ]]; then
  echo "Usage: boot <image name> <your name for this instance> [instance type]"
  exit 1
fi

if [ -z $INSTANCE_TYPE ]; then
  INSTANCE_TYPE="t2.micro"
fi


STACKINFO=$(aws --profile devbox cloudformation describe-stacks | jq ".Stacks[] | select(.StackName==\"$STACK_NAME\") | .Outputs")

IMAGE_ID=$(aws --profile devbox ec2 describe-images --filters "Name=tag:Application,Values=$STACK_NAME" "Name=name,Values=$IMAGE_NAME" | jq -r .Images[].ImageId)

if [ -z $IMAGE_ID ]; then
  # Assume it's a straight image ID and check if it exists:
  if [ $(aws --profile devbox ec2 describe-images --image-ids $IMAGE_NAME | jq ".[] | length") -eq 1 ]; then
    IMAGE_ID=$IMAGE_NAME
  else
    echo "Failed: Image $IMAGE_NAME not found."
    exit 1
  fi
fi

if (( $(grep -c . <<<"$IMAGE_ID") > 1 )); then 
  echo "Failed: Multiple images named '$IMAGE_NAME':"
  echo "$IMAGE_ID"
  exit 1
fi

SECURITY_GROUP=$(echo $STACKINFO | jq -r '.[] | select(.OutputKey=="SecurityGroup") | .OutputValue')
SUBNET=$(echo $STACKINFO | jq -r '.[] | select(.OutputKey=="Subnet") | .OutputValue')

INSTANCE_ID=$(aws --profile devbox ec2 run-instances --image-id $IMAGE_ID --count 1 --instance-type $INSTANCE_TYPE --key-name default-ssh-key --security-group-ids $SECURITY_GROUP --subnet-id $SUBNET --associate-public-ip-address | jq -r '.Instances[0].InstanceId')
aws --profile devbox ec2 create-tags --resources $INSTANCE_ID --tags Key=Application,Value=$STACK_NAME Key=ResourceName,Value=$INSTANCE_NAME

echo "Booting instance $INSTANCE_ID from image $IMAGE_ID..."

aws --profile devbox ec2 wait instance-running --instance-ids $INSTANCE_ID

IP_ADDRESS=$(aws --profile devbox ec2 describe-instances --instance-ids $INSTANCE_ID | jq -r .Reservations[].Instances[].PublicIpAddress)

echo "Instance $INSTANCE_ID running on $IP_ADDRESS"
