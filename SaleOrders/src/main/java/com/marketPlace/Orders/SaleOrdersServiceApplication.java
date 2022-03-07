package com.marketPlace.Orders;

import com.marketPlace.Orders.service.ReceiveAllPackages;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SaleOrdersServiceApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(SaleOrdersServiceApplication.class, args);
        context.getBean("receiveAllPackages", ReceiveAllPackages.class).receiveAllPackagesFromItemStorage();
    }
}
