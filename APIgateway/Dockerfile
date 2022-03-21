FROM openjdk
ARG JAR_FILE=build/libs/APIgateway.jar
RUN useradd -ms /bin/bash evgeny88
RUN mkdir -p /home/evgeny88/gateway-service/
WORKDIR /home/evgeny88/gateway-service/
RUN chown -R evgeny88:evgeny88 ./
COPY ${JAR_FILE} /APIgateway-0.1.jar
USER evgeny88
EXPOSE 8080
ENTRYPOINT ["java","-jar","/APIgateway-0.1.jar"]
