package com.marketplace.itemstorageservice.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaContainerConfig extends KafkaContainer{

    private static final Logger log = LoggerFactory.getLogger(KafkaContainerConfig.class);
    private static final String IMAGE_VERSION = "confluentinc/cp-kafka:7.0.0";
    private static KafkaContainerConfig kafkaContainer;

    private KafkaContainerConfig() {
        super(DockerImageName.parse(IMAGE_VERSION));
    }

    public static KafkaContainerConfig getContainer() {
        if (kafkaContainer == null) {
            kafkaContainer = new KafkaContainerConfig();
            kafkaContainer.start();
            kafkaContainer.setupSpringProperties();
            log.info("kafka container is started!>>>>");
        }
        return kafkaContainer;
    }

    public void setupSpringProperties() {
        String address = kafkaContainerAddress();
        setupBrokerAddress(address);
    }

    private static String kafkaContainerAddress() {
        return kafkaContainer.getBootstrapServers();
    }

    private static void setupBrokerAddress(String address) {
        System.setProperty("spring.kafka.bootstrap-servers", address);
    }
}
