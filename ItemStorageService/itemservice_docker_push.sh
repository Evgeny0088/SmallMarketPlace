#!/bin/bash
LAUGH="\U0001f60A";
UPSET="\U0001f62D";
FIRED="\U0001f525";
DOCKER="\U0001f433";
JAR="\U0001f680";
DOCKER_IMAGE='evgeny88docker/itemstorage-service:1.0'

echo -e "start to create jar file... ${JAR}"

if ! ./gradlew -x test clean build --no-parallel; then
   echo -e "fails to create a jar file... ${UPSET}"
else
   echo -e "jar is created!... ${LAUGH}\n"
   sleep 1
fi

echo -e "start to create docker image... ${DOCKER}\n"

docker image inspect ${DOCKER_IMAGE} >/dev/null 2>&1 && answer=yes || no
if [[ ${answer} == "yes" ]]; then
   echo -e "remove old image firstly...${FIRED}\n"
   docker rmi -f "${DOCKER_IMAGE}"
   echo -e "\n"
fi

if ! docker build -t "${DOCKER_IMAGE}" .; then
   echo -e "fails to create docker image... ${UPSET}"
else
   echo -e "image created!... ${LAUGH}\n"
fi

echo -e "pushing to dockerhub... ${DOCKER}"

if ! docker "login" || ! docker push "${DOCKER_IMAGE}"; then
   echo -e "failed to push image to dockerhub... ${UPSET}"
else
   echo -e "image pushed to dockerhub successfully!... ${LAUGH}" && set -e
fi


