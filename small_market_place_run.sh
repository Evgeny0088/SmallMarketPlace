#!/bin/bash
LAUGH='\U0001f602';
echo -e "hi, welcome to small market place${LAUGH}"
docker-compose -f servers.yml -f services.yml up && services.yml down --rmi local


