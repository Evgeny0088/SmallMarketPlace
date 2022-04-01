package com.marketplace.itemstorageservice.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@Testcontainers
public class BrandServiceTestConfig{

    private static final String IMAGE_VERSION = "confluentinc/cp-kafka:7.0.0";

    @Bean
    NewTopic userData() {
        return new NewTopic("user-data", 1, (short) 1);
    }

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = CustomPostgresSQLContainer.getInstance();

    @Container
    public static KafkaContainerConfig kafkaContainer;

    @BeforeAll
    public static void init(){
        postgreSQLContainer = CustomPostgresSQLContainer.getInstance();
        kafkaContainer = KafkaContainerConfig.getContainer(DockerImageName.parse(IMAGE_VERSION));
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            postgreSQLContainer.start();
            applicationContext.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent) {
                    postgreSQLContainer.stop();
                    kafkaContainer.stop();
                }
            });
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
