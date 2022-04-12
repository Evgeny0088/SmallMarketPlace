FROM openjdk
ARG JAR_FILE=build/libs/saleorders-service-boot-1.0.jar
RUN useradd -ms /bin/bash evgeny88
RUN mkdir -p /home/evgeny88/SaleOrdersService/
WORKDIR /home/evgeny88/SaleOrdersService/
RUN chown -R evgeny88:evgeny88 ./
COPY ${JAR_FILE} /saleorders-service-boot-1.0.jar
USER evgeny88
EXPOSE 8082
ENTRYPOINT ["java","-jar","/saleorders-service-boot-1.0.jar"]
