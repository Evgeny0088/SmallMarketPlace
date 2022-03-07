package com.marketPlace.Orders.configs;

import com.marketPlace.Orders.DTOModels.ItemSoldDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Configuration
public class ItemSoldKafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Value("${spring.kafka.producer.client-id}")
    private String producer_group_id;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Bean
    public ProducerFactory<String, ItemSoldDTO> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG,producer_group_id);
        configProps.put(ACKS_CONFIG,acks);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @Qualifier("itemSoldTemplate")
    public KafkaTemplate<String, ItemSoldDTO> itemSoldTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
