FROM openjdk
ARG JAR_FILE=build/libs/SaleOrdersService-0.1.jar
RUN useradd -ms /bin/bash evgeny88
RUN mkdir -p /home/evgeny88/SaleOrdersService/
WORKDIR /home/evgeny88/SaleOrdersService/
RUN chown -R evgeny88:evgeny88 ./
COPY ${JAR_FILE} /SaleOrdersService-0.1.jar 
USER evgeny88
EXPOSE 8082
ENTRYPOINT ["java","-jar","/SaleOrdersService-0.1.jar"]
