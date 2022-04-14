FROM openjdk
ARG JAR_FILE=build/libs/registry-service-boot-1.0.jar
RUN useradd -ms /bin/bash evgeny88
RUN mkdir -p /home/evgeny88/serviceRegistry/
WORKDIR /home/evgeny88/serviceRegistry/
RUN chown -R evgeny88:evgeny88 ./
COPY ${JAR_FILE} /registry-service-boot-1.0.jar
USER evgeny88
EXPOSE 8761
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=eurekaserver", "/registry-service-boot-1.0.jar"]
