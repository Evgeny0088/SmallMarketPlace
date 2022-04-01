package com.marketplace.itemstorageservice.configs;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;

import java.util.Map;

@TestConfiguration
public class WireMockConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Bean
    public WireMockServer wireMockServer(){
        return new WireMockServer();
    }

    @BeforeEach
    void clearWireMock() {
        wireMockServer().resetAll();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        wireMockServer().start();
        applicationContext.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent){
                wireMockServer().stop();
            }
        });
        applicationContext.getBeanFactory()
                .registerSingleton("brandWireMock", wireMockServer());
        TestPropertyValues.of(Map.of(
                "base_url", wireMockServer().baseUrl()))
                .applyTo(applicationContext);
    }
}
