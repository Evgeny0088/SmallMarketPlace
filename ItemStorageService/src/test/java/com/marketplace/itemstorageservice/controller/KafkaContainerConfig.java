package com.marketplace.itemstorageservice.controller;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

public class KafkaContainerConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaContainerConfig.class);

    private final static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.0"));
    private static String bootstrapServers;

    public static void start(Collection<NewTopic> topics) throws ExecutionException, InterruptedException, TimeoutException {
        kafka.start();
        bootstrapServers = kafka.getBootstrapServers();
        log.info("topics creation...");
//        try (var admin = AdminClient.create(consumerConfigs())) {
//            var result = admin.createTopics(topics);
//            for(var topicResult: result.values().values()) {
//                topicResult.get(10, TimeUnit.SECONDS);
//            }
//        }
        log.info("topics created");
    }

    public static void stop(){
        kafka.stop();
    }

    public static Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "item_storage_service");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return props;
    }
    public static String getBootstrapServers() {
        return bootstrapServers;
    }
}
