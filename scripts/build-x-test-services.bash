#!/usr/bin/env bash

eval $(minikube docker-env)

gradle build -x test
docker-compose build