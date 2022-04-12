FROM openjdk
ARG JAR_FILE=build/libs/itemStorage-service-boot-1.0.jar
RUN useradd -ms /bin/bash evgeny88
RUN mkdir -p /home/evgeny88/itemStorageService/
WORKDIR /home/evgeny88/itemStorageService/
RUN chown -R evgeny88:evgeny88 ./
COPY ${JAR_FILE} /itemStorage-service-boot-1.0.jar
USER evgeny88
EXPOSE 8081
ENTRYPOINT ["java","-jar","/itemStorage-service-boot-1.0.jar"]
