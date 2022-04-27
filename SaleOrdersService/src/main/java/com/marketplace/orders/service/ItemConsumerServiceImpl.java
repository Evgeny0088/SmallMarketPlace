package com.marketplace.orders.service;

import com.marketplace.orders.DTOModels.ItemDetailedInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ItemConsumerServiceImpl implements ItemConsumerService<String, ItemDetailedInfoDTO>{

    private final RedisTemplate<String, ItemDetailedInfoDTO> itemPackagesCache;
    private static final String REDIS_CACHE_KEY = "itemPackages";

    @Autowired
    public ItemConsumerServiceImpl(@Qualifier("ItemDetailedDTOPackages")
                                           RedisTemplate<String, ItemDetailedInfoDTO> itemPackagesCache) {
        this.itemPackagesCache = itemPackagesCache;
    }

    @KafkaListener(topics = {"${itemDTO.topic.name_1}"}, groupId = "${spring.kafka.consumer.client-id}",
                                                containerFactory = "ItemDetailedDTOUpdateConsumerFactory")
    public void receiveAllPackagesFromItemStorage(List<ItemDetailedInfoDTO> itemList) {
        if (!itemList.isEmpty()){
            redisTransactionalAction(itemList, itemPackagesCache);
            log.info("itemCache is loaded!...{} packages are inserted",itemList.size());
        }
        else {
            log.warn("no new packages from ItemStorage service!...");
        }
    }

    @KafkaListener(topics = {"${itemDTO.topic.name_2}", "${itemDTO.topic.name_3}"}, groupId = "${spring.kafka.consumer.client-id}",
            containerFactory = "ItemDetailedDTOUpdateConsumerFactory")
    public ItemDetailedInfoDTO updateItemDetailedDTOInCache(List<ItemDetailedInfoDTO> items) {
        if (!items.isEmpty()){
            redisTransactionalAction(items, itemPackagesCache);
            log.info("ItemDetailedDTO is updated in cache id:<{}>>>> \n{}", items.get(0).getItemPackageId(), items.get(0));
        }else {
            log.warn("no updates from itemStorage service! ...");
            return null;
        }
        return items.get(0);
    }

    @Override
    public void redisTransactionalAction(List<ItemDetailedInfoDTO> itemList, RedisTemplate<String, ItemDetailedInfoDTO> template) {
        template.execute(new SessionCallback<ItemDetailedInfoDTO>() {
            @Override
            public <K,V> ItemDetailedInfoDTO execute(RedisOperations<K,V> operations) throws DataAccessException {
                operations.multi();
                itemList.forEach(item-> itemPackagesCache.opsForHash().put(REDIS_CACHE_KEY, String.valueOf(item.getItemPackageId()),item));
                operations.exec();
                return null;
            }
        });
    }
}