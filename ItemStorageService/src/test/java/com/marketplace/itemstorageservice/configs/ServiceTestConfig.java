package com.marketplace.itemstorageservice.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestConfiguration
@Testcontainers
public class ServiceTestConfig {

    @Bean
    NewTopic userData() {
        return new NewTopic("user-data", 1, (short) 1);
    }

    @Container
    public static CustomPostgresSQLContainer postgreSQLContainer = CustomPostgresSQLContainer.getInstance();

    @Container
    public static KafkaContainerConfig kafkaContainer = KafkaContainerConfig.getContainer();

    @Container
    public static RedisCustomContainer redisCustomContainer = RedisCustomContainer.getInstance();

    @AfterAll
    public static void destroy(){
        postgreSQLContainer.stop();
        kafkaContainer.stop();
        redisCustomContainer.stop();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            applicationContext.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent) {
                    postgreSQLContainer.stop();
                    kafkaContainer.stop();
                    redisCustomContainer.stop();
                }
            });
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                    "spring.redis.host=" + redisCustomContainer.getContainerIpAddress(),
                    "spring.redis.port=" + redisCustomContainer.getMappedPort(redisCustomContainer.getRedisPort())
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
