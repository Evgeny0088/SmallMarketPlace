package com.marketplace.itemstorageservice.configs.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.models.ItemType;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ItemRedisSerializer extends JdkSerializationRedisSerializer {

    private final CustomSerializer customSerializer = new CustomSerializer() {
        @Override
        public Item doDeserialization(byte[] data) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
            mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
            mapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
            DateTimeFormatter form = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Item item = new Item();
            try {
                JsonNode node = mapper.readTree(data);
                item.setId(node.get("id").asLong());
                item.setSerial(node.get("serial").asLong());
                item.setItem_type(ItemType.valueOf(node.get("item_type").asText()));
                JsonNode parent = node.get("parentItem");
                if (parent.isNull()){
                    item.setParentItem(null);
                }else{
                    Item parentItem = new Item();
                    parentItem.setId(parent.asLong());
                    item.setParentItem(parentItem);
                }
                item.setCreationDate(LocalDateTime.parse(node.get("creationDate").asText(), form));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return item;
        }
    };

    @Override
    public Object deserialize(byte[] bytes) {
        return customSerializer.doDeserialization(bytes);
    }

    @Override
    public byte[] serialize(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try{
            return mapper.writeValueAsBytes(object);
        }catch (JsonProcessingException exception){
            throw new SerializationException(exception.getMessage(),exception);
        }
    }
}