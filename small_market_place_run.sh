#!/bin/bash
LAUGH='\U0001f602';
echo -e "hi, welcome to small market place${LAUGH}"
docker-compose -f docker-compose-servers.yml -f docker-compose-services.yml up && docker-compose-services.yml down --rmi local


