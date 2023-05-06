#!/usr/bin/env bash

eval $(minikube docker-env)

docker-compose up -d keycloak