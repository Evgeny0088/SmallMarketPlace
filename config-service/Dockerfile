FROM openjdk
ARG JAR_FILE=build/libs/config-service-boot-1.0.jar
RUN useradd -ms /bin/bash evgeny88
RUN mkdir -p /home/evgeny88/config-service/
WORKDIR /home/evgeny88/config-service/
RUN chown -R evgeny88:evgeny88 ./
COPY ${JAR_FILE} /config-service-boot-1.0.jar
USER evgeny88
EXPOSE 9000
ENTRYPOINT ["java","-jar","/config-service-boot-1.0.jar"]
