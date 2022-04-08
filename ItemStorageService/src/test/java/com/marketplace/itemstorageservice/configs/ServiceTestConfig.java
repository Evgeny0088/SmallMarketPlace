package com.marketplace.itemstorageservice.configs;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestConfiguration
@Testcontainers
public class ServiceTestConfig {

    @Container
    public static CustomPostgresSQLContainer postgresSQLContainer = CustomPostgresSQLContainer.getInstance();

    @Container
    public static KafkaContainerConfig kafkaContainer = KafkaContainerConfig.getContainer();

    @Container
    public static RedisCustomContainer redisCustomContainer = RedisCustomContainer.getInstance();

    @AfterAll
    public static void destroy(){
        postgresSQLContainer.stop();
        redisCustomContainer.stop();
        kafkaContainer.stop();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            applicationContext.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent) {
                    postgresSQLContainer.stop();
                    kafkaContainer.stop();
                    redisCustomContainer.stop();
                }
            });
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgresSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgresSQLContainer.getPassword(),
                    "spring.redis.host=" + redisCustomContainer.getContainerIpAddress(),
                    "spring.redis.port=" + redisCustomContainer.getMappedPort(redisCustomContainer.getRedisPort())
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
