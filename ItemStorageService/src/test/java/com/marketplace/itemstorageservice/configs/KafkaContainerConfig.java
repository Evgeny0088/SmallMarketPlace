package com.marketplace.itemstorageservice.configs;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestComponent
public class KafkaContainerConfig extends KafkaContainer{

    private static final Logger log = LoggerFactory.getLogger(KafkaContainerConfig.class);
    private static final String IMAGE_VERSION = "confluentinc/cp-kafka:7.0.0";
    private static KafkaContainerConfig kafkaContainer;

    @Autowired
    @Qualifier("updateItemRequest")
    NewTopic updateItemTopic;

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

    public KafkaMessageListenerContainer<String, List<ItemDetailedInfoDTO>> getMessageContainer(){
        DefaultKafkaConsumerFactory<String, List<ItemDetailedInfoDTO>> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig());
        return new KafkaMessageListenerContainer<>(consumerFactory, getContainerProperties());
    }

    private Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ItemDetailedDTOKafkaDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sale_orders_client_id");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return props;
    }

    private ContainerProperties getContainerProperties(){
        return new ContainerProperties("update_item");
    }
}
