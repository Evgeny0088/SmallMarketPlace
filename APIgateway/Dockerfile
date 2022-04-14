FROM openjdk
ARG JAR_FILE=build/libs/gateway-service-boot-1.0.jar
RUN useradd -ms /bin/bash evgeny88
RUN mkdir -p /home/evgeny88/gateway-service/
WORKDIR /home/evgeny88/gateway-service/
RUN chown -R evgeny88:evgeny88 ./
COPY ${JAR_FILE} /gateway-service-boot-1.0.jar
USER evgeny88
EXPOSE 8080
ENTRYPOINT ["java","-jar","/gateway-service-boot-1.0.jar"]
