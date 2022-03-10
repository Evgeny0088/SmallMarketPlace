package com.marketPlace.Orders.configs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketPlace.Orders.DTOModels.ItemDetailedInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ItemDetailedDTODeserializer implements Deserializer<List<ItemDetailedInfoDTO>> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public List<ItemDetailedInfoDTO> deserialize(String topic, byte[] data){
        List<ItemDetailedInfoDTO> items = new ArrayList<>();
        try {
            JsonNode node = mapper.readTree(data);
            if (node.get(0) == null){
                log.error("Input data is empty or wrong from ItemStorage service!...");
                return items;
            }
            for (int i = 0; i<node.size(); i++){
                items.add(new ItemDetailedInfoDTO(
                        node.get(i).get("itemPackageId").asLong(),
                        node.get(i).get("serial").asLong(),
                        node.get(i).get("itemsQuantityInPack").asLong(),
                        node.get(i).get("brandName").asText(),
                        node.get(i).get("brandVersion").asText())
                        );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

}