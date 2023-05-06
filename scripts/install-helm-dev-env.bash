#!/usr/bin/env bash

helm install streamify-dev-env \
  k8s/helm/environments/dev-env \
  -n streamify \
  --create-namespace