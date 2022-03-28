package com.marketplace.itemstorageservice.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Map;

@TestConfiguration
public class WireMockConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Value("${brandWireMockServer}")
    private int PORT;

    private WireMockServer brandWireMockServer;

    @Bean
    public WireMockServer brandWireMockServer(){
        return new WireMockServer(PORT);
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        brandWireMockServer.start();
        applicationContext.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent){
                brandWireMockServer.stop();
            }
        });
        applicationContext.getBeanFactory()
                .registerSingleton("brandWireMockServer", brandWireMockServer);
        TestPropertyValues.of(Map.of("brand_base_url", brandWireMockServer.baseUrl()))
                .applyTo(applicationContext);
    }
}
