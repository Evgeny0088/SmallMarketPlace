#!/bin/bash
LAUGH='\U0001f602';
echo -e "hi, welcome to small market place${LAUGH}"
DOCKER_IMAGE='evgeny88docker/itemstorage-service:1.0'

if ! docker-compose pull ; then
   echo -e "fails to create a jar file... ${UPSET}"
else
   echo -e "jar is created!... ${LAUGH}\n"
   sleep 1
fi


