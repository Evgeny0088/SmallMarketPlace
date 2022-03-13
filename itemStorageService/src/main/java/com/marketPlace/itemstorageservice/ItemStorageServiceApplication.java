package com.marketPlace.itemstorageservice;

import com.marketPlace.itemstorageservice.services.LoadAllPackages;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@OpenAPIDefinition
@Slf4j
public class ItemStorageServiceApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ItemStorageServiceApplication.class, args);
        context.getBean("allPackagesLoader", LoadAllPackages.class).loadAllItemsFromDB();
    }
}
