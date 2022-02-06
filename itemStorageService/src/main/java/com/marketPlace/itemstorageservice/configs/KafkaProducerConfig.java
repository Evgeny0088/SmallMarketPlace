package com.marketPlace.itemstorageservice.configs;

import com.marketPlace.itemstorageservice.DTOModels.ItemDetailedInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Value("${spring.kafka.producer.client-id}")
    private String producerClientId;

    public Map<String, Object> producerProperties(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaServer);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, producerClientId);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return properties;
    }

    @Bean
    ProducerFactory<String, ItemDetailedInfoDTO> updateItemProducerFactory(){
        return new DefaultKafkaProducerFactory<>(producerProperties());
    }

    @Bean
    ProducerFactory<String, List<ItemDetailedInfoDTO>> updateAllPackagesProducerFactory(){
        return new DefaultKafkaProducerFactory<>(producerProperties());
    }

    @Bean
    KafkaTemplate<String, ItemDetailedInfoDTO> updateItemKafkaTemplate(
            ProducerFactory<String, ItemDetailedInfoDTO> updateItemProducerFactory){
        log.info("produser is created!");
        return new KafkaTemplate<>(updateItemProducerFactory);
    }

    @Bean
    KafkaTemplate<String, List<ItemDetailedInfoDTO>> updateAllPackagesKafkaTemplate(
            ProducerFactory<String, List<ItemDetailedInfoDTO>> updateAllPackagesProducerFactory){
        return new KafkaTemplate<>(updateAllPackagesProducerFactory);
    }

}
