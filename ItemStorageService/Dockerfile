ARG DOCKERHUB=docker.io

FROM ${DOCKERHUB}/openjdk:17-alpine
ARG JAR_FILE=build/libs/itemstorage-service-boot-1.0.jar
RUN mkdir -p /home/evgeny88/itemStorageService/
WORKDIR /home/evgeny88/itemStorageService/
COPY ${JAR_FILE} /itemstorage-service-boot-1.0.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=dev", "/itemstorage-service-boot-1.0.jar"]
