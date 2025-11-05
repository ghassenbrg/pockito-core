#!/bin/bash

PROFILE=${1:-dev}

echo "Starting Pockito Core with profile: $PROFILE"

./mvnw spring-boot:run -Dspring-boot.run.profiles=$PROFILE

