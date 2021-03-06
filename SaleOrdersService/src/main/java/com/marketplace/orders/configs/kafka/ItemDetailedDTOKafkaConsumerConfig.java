package com.marketplace.orders.configs.kafka;

import com.marketplace.orders.DTOModels.ItemDetailedInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class ItemDetailedDTOKafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Value("${spring.kafka.consumer.client-id}")
    private String consumer_group_id;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ItemDetailedDTOKafkaDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumer_group_id);
        props.put(ConsumerConfig.DEFAULT_ISOLATION_LEVEL, "read_committed");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG,"read_committed");
        return props;
    }

    @Bean
    public KafkaListenerContainerFactory<?> kafkaListenerContainerFactory() {
        return new ConcurrentKafkaListenerContainerFactory<>();
    }

    @Bean
    public ConsumerFactory<String, List<ItemDetailedInfoDTO>> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /*
    factory for getting any updates of ItemDetailedDTO from itemStorage service database
    */
    @Bean(name = "ItemDetailedDTOUpdateConsumerFactory")
    public KafkaListenerContainerFactory<?> ItemDetailedDTOUpdateConsumerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, List<ItemDetailedInfoDTO>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(false);
        return factory;
    }

    @Bean
    public ItemDetailedDTOKafkaDeserializer converter() {
        return new ItemDetailedDTOKafkaDeserializer();
    }

}
