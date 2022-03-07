package com.marketPlace.Orders.configs;

import com.marketPlace.Orders.DTOModels.ItemDetailedInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class ItemDetailedDTOKafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Value("${spring.kafka.producer.client-id}")
    private String consumer_group_id;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringJsonMessageConverter.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumer_group_id);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return props;
    }

    @Bean
    public KafkaListenerContainerFactory<?> kafkaListenerContainerFactory() {
        return new ConcurrentKafkaListenerContainerFactory<>();
    }

    /*
    consumer factory for getting ItemDetailedDTO list from database
     */
    @Bean
    public ConsumerFactory<String, List<ItemDetailedInfoDTO>> consumerListFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /*
    consumer factory for getting updated ItemDetailedDTO from database (created/updated/deleted)
     */
    @Bean
    public ConsumerFactory<String, ItemDetailedInfoDTO> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /*
    factory for getting ItemDetailedDTO list from database
    */
    @Bean
    @Qualifier("itemDetailedDTOListConsumerFactory")
    public KafkaListenerContainerFactory<?> itemDetailedDTOListConsumerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, List<ItemDetailedInfoDTO>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerListFactory());
        factory.setBatchListener(false);
        factory.setMessageConverter(new StringJsonMessageConverter());
        return factory;
    }

    /*
    factory for getting ItemDetailedDTO from database (created/updated/deleted)
    */
    @Bean
    @Qualifier("itemDetailedDTOConsumerFactory")
    public KafkaListenerContainerFactory<?> itemDetailedDTOConsumerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ItemDetailedInfoDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(false);
        factory.setMessageConverter(new StringJsonMessageConverter());
        return factory;
    }

    @Bean
    public StringJsonMessageConverter messageConverter(){
        return new StringJsonMessageConverter();
    }
    @Bean
    public ItemDetailedDTODeserializer converter() {
        return new ItemDetailedDTODeserializer();
    }
}
