FROM openjdk
ARG JAR_FILE=build/libs/itemStorageService-0.1.jar
RUN useradd -ms /bin/bash evgeny88
RUN mkdir -p /home/evgeny88/itemStorageService/
WORKDIR /home/evgeny88/itemStorageService/
RUN chown -R evgeny88:evgeny88 ./
COPY ${JAR_FILE} /itemStorageService-0.1.jar  
USER evgeny88
EXPOSE 8081
ENTRYPOINT ["java","-jar","/itemStorageService-0.1.jar"]
