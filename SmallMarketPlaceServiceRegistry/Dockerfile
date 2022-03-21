FROM openjdk
ARG JAR_FILE=build/libs/ServiceRegistry.jar
RUN useradd -ms /bin/bash evgeny88
RUN mkdir -p /home/evgeny88/serviceRegistry/
WORKDIR /home/evgeny88/serviceRegistry/
RUN chown -R evgeny88:evgeny88 ./
COPY ${JAR_FILE} /ServiceRegistry-0.1.jar
USER evgeny88
EXPOSE 8761
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=eurekaserver", "/ServiceRegistry-0.1.jar"]
