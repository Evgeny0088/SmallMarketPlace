#!/bin/bash
UPLOAD='\U0001f680';
LAUGH='\U0001f602';
UPSET="\U0001f62D";
DOCKER='\U0001f433';
DOCKER_HUB='evgeny88docker';

declare -a images=("${DOCKER_HUB}/registry-service:1.0"
                   "${DOCKER_HUB}/config-service:1.0"
                   "${DOCKER_HUB}/gateway-service:1.0"
                   "${DOCKER_HUB}/itemstorage-service:1.0"
                   "${DOCKER_HUB}/saleorders-service:1.0")

echo -e "hi, welcome to small market place!...${LAUGH}\n"
sleep 1

echo -e "Starting to load service images from repositoty...${UPLOAD}\n"

for image in "${images[@]}";
    do
      if ! docker pull ${image}; then
         echo -e "${image} image not found >>> please create new image!...${UPSET}\n"
         set -e
      else
         echo -e "image found!... ${DOCKER}\n"
      fi
    done

docker-compose -f servers.yml -f services.yml up --no-build &&\
docker-compose -f services.yml down --rmi local &&\
docker-compose -f servers.yml down --rmi local


