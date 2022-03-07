package com.marketPlace.itemstorageservice.configs;

import com.marketPlace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketPlace.itemstorageservice.DTOmodels.ItemSoldDTO;
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
    producer factory for transfer all Items from DB
     */
    @Bean
    public ProducerFactory<String, List<ItemDetailedInfoDTO>> producerListFactory() {
        Map<String, Object> configProps = producerSetup();
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /*
    producer factory for transfer updated items (created/updated/deleted)
     */
    @Bean
    public ProducerFactory<String, ItemDetailedInfoDTO> producerFactory() {
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

    @Bean
    @Qualifier("allItemsListProducer")
    public KafkaTemplate<String, List<ItemDetailedInfoDTO>> allItemsListProducer() {
        return new KafkaTemplate<>(producerListFactory());
    }

    @Bean
    @Qualifier("singleItemProducer")
    public KafkaTemplate<String, ItemDetailedInfoDTO> singleItemProducer() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    @Qualifier("itemCountReductionInPackage")
    public KafkaTemplate<String, ItemSoldDTO> itemCountReduction(){
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
