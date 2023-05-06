#!/usr/bin/env bash

for f in k8s/helm/components/*; do helm dep up $f; done
for f in k8s/helm/environments/*; do helm dep up $f; done
helm dep ls k8s/helm/environments/dev-env/