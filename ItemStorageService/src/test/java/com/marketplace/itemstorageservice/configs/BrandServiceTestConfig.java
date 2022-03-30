package com.marketplace.itemstorageservice.configs;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConfiguration
@Testcontainers
public class BrandServiceTestConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Bean
    public PostgreSQLContainer<?> postgreSQLContainer(){
        return new PostgreSQLContainer<>("postgres:9.4");
    }

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        postgreSQLContainer().start();
        applicationContext.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent){
                postgreSQLContainer().stop();
            }
        });
        TestPropertyValues.of(
                "spring.datasource.url=" + postgreSQLContainer().getJdbcUrl(),
                "spring.datasource.username=" + postgreSQLContainer().getUsername(),
                "spring.datasource.password=" + postgreSQLContainer().getPassword()
        ).applyTo(applicationContext.getEnvironment());

    }
}
