package com.marketplace.itemstorageservice;

import com.marketplace.itemstorageservice.services.LoadAllPackages;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableEurekaClient
@OpenAPIDefinition
public class ItemStorageServiceApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ItemStorageServiceApplication.class, args);
        context.getBean("allPackagesLoader", LoadAllPackages.class).loadAllItemsFromDB();
    }
}
