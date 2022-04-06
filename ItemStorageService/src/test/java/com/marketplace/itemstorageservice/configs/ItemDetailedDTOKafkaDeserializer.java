package com.marketplace.itemstorageservice.configs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemDetailedDTOKafkaDeserializer implements Deserializer<List<ItemDetailedInfoDTO>> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public List<ItemDetailedInfoDTO> deserialize(String topic, byte[] data){
        ObjectMapper mapper = new ObjectMapper();
        List<ItemDetailedInfoDTO> items = new ArrayList<>();
        try {
            JsonNode node = mapper.readTree(data);
            if (node.get(0) == null){
                return items;
            }
            for (int i = 0; i<node.size(); i++){
                items.add(itemMapper(node.get(i)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    private ItemDetailedInfoDTO itemMapper(JsonNode node){
        return new ItemDetailedInfoDTO(
                node.get("itemPackageId").asLong(),
                node.get("serial").asLong(),
                node.get("itemsQuantityInPack").asLong(),
                node.get("brandName").asText(),
                node.get("brandVersion").asText());
    }
}