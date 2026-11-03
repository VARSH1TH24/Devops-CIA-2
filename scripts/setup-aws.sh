#!/usr/bin/env bash
# One-time cloud setup: ECR repository + EKS cluster. Requires aws CLI and eksctl, already authenticated.
set -euo pipefail
REGION=${AWS_REGION:-ap-south-1}
CLUSTER=${EKS_CLUSTER_NAME:-cicd-demo-cluster}

aws ecr create-repository --repository-name cicd-java-app --region "$REGION" \
  --image-scanning-configuration scanOnPush=true || echo "ECR repo already exists"

eksctl create cluster \
  --name "$CLUSTER" --region "$REGION" \
  --nodegroup-name workers --node-type t3.medium \
  --nodes 2 --nodes-min 2 --nodes-max 3 --managed

aws eks update-kubeconfig --name "$CLUSTER" --region "$REGION"
kubectl get nodes

echo "Add these GitHub repository secrets: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION=$REGION, EKS_CLUSTER_NAME=$CLUSTER"
echo "To avoid charges afterwards: eksctl delete cluster --name $CLUSTER --region $REGION"
