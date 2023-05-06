#!/usr/bin/env bash

unset KUBECONFIG

minikube start \
  --profile=streamify \
  --memory=10240 \
  --cpus=8 \
  --disk-size=30g \
  --kubernetes-version=v1.26.0 \
  --driver=docker \
  --ports=8080:80 --ports=8443:443 \
  --ports=30080:30080 --ports=30443:30443

minikube profile streamify

minikube addons enable ingress
minikube addons enable metrics-server