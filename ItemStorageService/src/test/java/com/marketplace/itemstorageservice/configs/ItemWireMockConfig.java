package com.marketplace.itemstorageservice.configs;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Map;

@TestConfiguration
public class ItemWireMockConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Value("${itemWireMockServer}")
    private int PORT;

    @Bean("itemWireMock")
    public WireMockServer itemWireMockServer(){
        return new WireMockServer(PORT);
    }

    @BeforeEach
    void clearWireMock() {
        itemWireMockServer().resetAll();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        itemWireMockServer().start();
        applicationContext.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent){
                itemWireMockServer().stop();
            }
        });
        applicationContext.getBeanFactory()
                .registerSingleton("itemWireMock", itemWireMockServer());
        TestPropertyValues.of(Map.of(
                "item_base_url", itemWireMockServer().baseUrl()))
                .applyTo(applicationContext);
    }
}
