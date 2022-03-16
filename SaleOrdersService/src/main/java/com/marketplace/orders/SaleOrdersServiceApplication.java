package com.marketplace.orders;

import com.marketplace.orders.service.ReceiveAllPackages;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableEurekaClient
@OpenAPIDefinition
public class SaleOrdersServiceApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(SaleOrdersServiceApplication.class, args);
        context.getBean("receiveAllPackages", ReceiveAllPackages.class).receiveAllPackagesFromItemStorage();
    }
}
