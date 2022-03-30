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
public class BrandWireMockConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Value("${brandWireMockServer}")
    private int PORT;

    @Bean("brandWireMock")
    public WireMockServer brandWireMockServer(){
        return new WireMockServer(PORT);
    }

    @BeforeEach
    void clearWireMock() {
        brandWireMockServer().resetAll();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        brandWireMockServer().start();
        applicationContext.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent){
                brandWireMockServer().stop();
            }
        });
        applicationContext.getBeanFactory()
                .registerSingleton("brandWireMock", brandWireMockServer());
        TestPropertyValues.of(Map.of(
                "brand_base_url", brandWireMockServer().baseUrl()))
                .applyTo(applicationContext);
    }
}
