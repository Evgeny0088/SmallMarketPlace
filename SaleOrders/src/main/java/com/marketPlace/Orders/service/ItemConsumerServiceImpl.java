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
        itemList.forEach(item-> itemDTOCache.put(item.getItemPackageId(),item));
        log.info("itemCache is loaded!...{} packages inserted",itemList.size());
    }

    @Override
    @CachePut(value = "ItemDTO-Cache", condition = "#item != null ", key = "#item.itemPackageId")
    @KafkaListener(topics = {"${itemDTO.topic.name_2}", "${itemDTO.topic.name_3}"}, groupId = "${spring.kafka.consumer.client-id}", containerFactory = "itemDetailedDTOConsumerFactory")
    public ItemDetailedInfoDTO updateItemDetailedDTOInCache(ItemDetailedInfoDTO item) {
        if (item != null){
            log.info("ItemDetailedDTO is received >>> {}", item);
        }else {
            log.warn("no updates from itemStorage service! ...");
        }
        return item;
    }
}