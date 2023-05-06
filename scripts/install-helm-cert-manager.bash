#!/usr/bin/env bash

helm repo add jetstack https://charts.jetstack.io
helm repo update
helm install cert-manager jetstack/cert-manager \
  --create-namespace \
  --namespace cert-manager \
  --version v1.3.1 \
  --set installCRDs=true \
  --wait

# Verify installation
kubectl get pods --namespace cert-manager