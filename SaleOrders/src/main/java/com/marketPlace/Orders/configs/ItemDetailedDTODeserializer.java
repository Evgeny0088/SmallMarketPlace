package com.marketPlace.Orders.configs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketPlace.Orders.DTOModels.ItemDetailedInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class ItemDetailedDTODeserializer implements Deserializer<ItemDetailedInfoDTO> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public ItemDetailedInfoDTO deserialize(String topic, byte[] data){
        if (data == null){
            log.error("Input data is not available!...");
            return null;
        }
        try{
            JsonNode node = mapper.readTree(data);
            long itemPackId = node.get("itemPackId").asLong();
            long serial = node.get("serial").asLong();
            String brandName = node.get("brandName").asText();
            String brandVersion = node.get("brandVersion").asText();
            long itemCountInPack = node.get("itemCountInPack").asLong();
            return new ItemDetailedInfoDTO(itemPackId,serial,brandName,brandVersion,itemCountInPack);
        } catch (SerializationException | IOException e) {
            throw new SerializationException("Error when deserializing byte[] to ItemDetailedDto");
        }
    }
}