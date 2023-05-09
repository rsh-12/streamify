#!/usr/bin/env bash

eval $(minikube docker-env)

gradle build -x test
(cd microservices/streaming-service && npm run build)
docker-compose build