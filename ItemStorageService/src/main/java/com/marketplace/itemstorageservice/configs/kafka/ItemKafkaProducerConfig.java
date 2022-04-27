package com.marketplace.itemstorageservice.configs.kafka;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.DTOmodels.ItemSoldDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;

@Configuration
public class ItemKafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Value("${spring.kafka.producer.client-id}")
    private String producer_group_id;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    /*
    producer factory for transfer any updates on Items from database
     */
    @Bean
    public ProducerFactory<String, List<ItemDetailedInfoDTO>> producerListFactory() {
        Map<String, Object> configProps = producerSetup();
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /*
    producer factory for transfer updated count of items in package (when removed from database)
    */
    @Bean
    public ProducerFactory<String, ItemSoldDTO> itemCountProducerFactory() {
        Map<String, Object> configProps = producerSetup();
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(name = "ItemDetailedDTOUpdateProducer")
    public KafkaTemplate<String, List<ItemDetailedInfoDTO>> ItemDetailedDTOUpdateProducer() {
        return new KafkaTemplate<>(producerListFactory());
    }

    @Bean(name = "itemCountReductionInPackage")
    public KafkaTemplate<String, ItemSoldDTO> itemCountReductionInPackage(){
        return new KafkaTemplate<>(itemCountProducerFactory());
    }

    private Map<String, Object> producerSetup(){
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG,producer_group_id);
        configProps.put(ACKS_CONFIG,acks);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return configProps;
    }
}
