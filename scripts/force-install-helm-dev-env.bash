#!/usr/bin/env bash

# Recreate namespace
kubectl delete namespace streamify
kubectl apply -f k8s/streamify-namespace.yml
kubectl config set-context $(kubectl config current-context) --namespace=streamify

# Update charts
for f in k8s/helm/components/*; do helm dep up $f; done
for f in k8s/helm/environments/*; do helm dep up $f; done
helm dep ls k8s/helm/environments/dev-env/

# Install dev-env
helm install streamify-dev-env \
  k8s/helm/environments/dev-env \
  -n streamify \
  --create-namespace