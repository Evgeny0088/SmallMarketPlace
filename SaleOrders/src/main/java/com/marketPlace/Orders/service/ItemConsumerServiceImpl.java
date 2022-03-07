package com.marketPlace.Orders.service;

import com.marketPlace.Orders.DTOModels.ItemDetailedInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ItemConsumerServiceImpl implements ItemConsumerService<ItemDetailedInfoDTO>{

    private final Cache<Long, ItemDetailedInfoDTO> itemDTOCache;

    @Autowired
    public ItemConsumerServiceImpl(Cache<Long, ItemDetailedInfoDTO> itemDTOCache) {
        this.itemDTOCache = itemDTOCache;
    }

    @KafkaListener(topics = {"${itemDTO.topic.name_1}"}, groupId = "${spring.kafka.consumer.client-id}", containerFactory = "itemDetailedDTOListConsumerFactory")
    public void receiveAllPackagesFromItemStorage(List<ItemDetailedInfoDTO> itemList) {
        ItemDetailedInfoDTO t1 = new ItemDetailedInfoDTO(9L,100,"gucci","0.1",3);
        ItemDetailedInfoDTO t2 = new ItemDetailedInfoDTO(76L,100,"gucci","0.1",2);
        itemDTOCache.put(t1.getItemPackId(),t1);
        itemDTOCache.put(t1.getItemPackId(),t2);
//        itemList.forEach(item-> itemDTOCache.put(item.getItemPackId(),item));
    }

    @Override
    @CachePut(value = "ItemDTO-Cache", condition = "#item != null ", key = "#item.itemPackId")
    @KafkaListener(topics = {"${itemDTO.topic.name_2}", "${itemDTO.topic.name_3}"}, groupId = "${spring.kafka.consumer.client-id}", containerFactory = "itemDetailedDTOConsumerFactory")
    public ItemDetailedInfoDTO updateItemDetailedDTOInCache(ItemDetailedInfoDTO item) {
        if (item != null){
            log.info("ItemDetailedDTO is received >>> {}", item);
        }else {
            log.warn("no updates from database! ...");
        }
        return item;
    }
}