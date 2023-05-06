#!/usr/bin/env bash

helm upgrade --install istio-streamify-addons k8s/helm/environments/istio-system -n istio-system --wait