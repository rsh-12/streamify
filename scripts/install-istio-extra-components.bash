#!/usr/bin/env bash

istio_version=$(istioctl version --short --remote=false)
echo "Installing integrations for Istio v$istio_version"

# Install kiali
kubectl apply -n istio-system -f https://raw.githubusercontent.com/istio/istio/${istio_version}/samples/addons/kiali.yaml

# Install jaeger
kubectl apply -n istio-system -f https://raw.githubusercontent.com/istio/istio/${istio_version}/samples/addons/jaeger.yaml

# Install prometheus
kubectl apply -n istio-system -f https://raw.githubusercontent.com/istio/istio/${istio_version}/samples/addons/prometheus.yaml

# Install grafana
kubectl apply -n istio-system -f https://raw.githubusercontent.com/istio/istio/${istio_version}/samples/addons/grafana.yaml

# Verify installation
kubectl -n istio-system wait --timeout=600s --for=condition=available deployment --all

kubectl -n istio-system get deploy
