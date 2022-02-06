package com.marketPlace.Orders.configs;

import com.marketPlace.Orders.DTOModels.ItemDetailedInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Value("${spring.kafka.consumer.client-id}")
    private String consumerClientId;

    public Map<String, Object> producerProperties(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaServer);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerClientId);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"update_item_group");
        properties.put(JsonDeserializer.TRUSTED_PACKAGES,"*");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Deserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return properties;
    }

    @Bean
    public ConsumerFactory<String, ItemDetailedInfoDTO> updateItemDTOConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(producerProperties(),
                new StringDeserializer(), new JsonDeserializer<>(ItemDetailedInfoDTO.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ItemDetailedInfoDTO> updateItemDTOKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ItemDetailedInfoDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(updateItemDTOConsumerFactory());
        return factory;
    }

    @KafkaListener(topics = "${itemDTO.topic.name_2}", containerFactory = "updateItemDTOKafkaListenerContainerFactory")
    public void actionOnUpdate(ItemDetailedInfoDTO itemDetailedInfoDTO) {
        log.info(String.format("itemDTO is created -> %s with id<%d>",
                                itemDetailedInfoDTO,itemDetailedInfoDTO.getItemPackId()));
    }

}
