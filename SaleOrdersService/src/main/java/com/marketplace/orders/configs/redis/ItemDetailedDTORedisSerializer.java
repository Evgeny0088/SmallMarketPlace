package com.marketplace.orders.configs.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.orders.DTOModels.ItemDetailedInfoDTO;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import java.io.IOException;

public class ItemDetailedDTORedisSerializer extends JdkSerializationRedisSerializer {


    @Override
    public Object deserialize(byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(bytes);
            return new ItemDetailedInfoDTO(
                    node.get("itemPackageId").asLong(),
                    node.get("serial").asLong(),
                    node.get("itemsQuantityInPack").asLong(),
                    node.get("brandName").asText(),
                    node.get("brandVersion").asText());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] serialize(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try{
            return mapper.writeValueAsBytes(object);
        }catch (JsonProcessingException exception){
            throw new SerializationException(exception.getMessage(),exception);
        }
    }
}