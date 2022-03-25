package com.marketplace.orders.service;

import com.marketplace.orders.DTOModels.ItemDetailedInfoDTO;
import com.marketplace.orders.DTOModels.ItemSoldDTO;
import com.marketplace.orders.exceptions.CustomItemsException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class ItemProducerServiceImpl implements ItemProducerService {

    private final NewTopic itemSold;
    private final KafkaTemplate<Long, ItemSoldDTO> itemSoldTemplate;
    private final RedisTemplate<String, ItemDetailedInfoDTO> itemPackagesCache;
    private static final String REDIS_CACHE_KEY = "itemPackages";

    @Autowired
    public ItemProducerServiceImpl(@Qualifier("soldItemsRequest") NewTopic itemSold,
                                   @Qualifier("itemSoldTemplate") KafkaTemplate<Long, ItemSoldDTO> itemSoldTemplate,
                                   @Qualifier("ItemDetailedDTOPackages") RedisTemplate<String, ItemDetailedInfoDTO> itemPackagesCache) {
        this.itemSold = itemSold;
        this.itemSoldTemplate = itemSoldTemplate;
        this.itemPackagesCache = itemPackagesCache;
    }

    @Override
    public boolean sendSoldItem(long id, int quantity) {
        HashOperations<String, String, ItemDetailedInfoDTO> redisCache = itemPackagesCache.opsForHash();
        String str_id = String.valueOf(id);
        if (redisCache.hasKey(REDIS_CACHE_KEY, str_id)){
            ItemDetailedInfoDTO packageOfSoldItems = redisCache.get(REDIS_CACHE_KEY,str_id);
            log.info("ItemPackage retrieved from cache:{}...",packageOfSoldItems);
            long currentQuantity = packageOfSoldItems != null ? packageOfSoldItems.getItemsQuantityInPack()-quantity : -1;
            if (currentQuantity<0){
                log.warn("not enough items in current package: <{}>, need to check in DB...",currentQuantity);
                return false;
            }else {
                packageOfSoldItems.setItemsQuantityInPack(currentQuantity);
                ItemSoldDTO soldItems = new ItemSoldDTO(id,quantity);
                ListenableFuture<SendResult<Long, ItemSoldDTO>> future = itemSoldTemplate.send(itemSold.name(), soldItems);
                redisCache.put(REDIS_CACHE_KEY, str_id, packageOfSoldItems);
                log.info("########item is updated in cache####### >>>>>{}", redisCache.get(REDIS_CACHE_KEY, str_id));
                future.addCallback(new ListenableFutureCallback<>(){
                    @Override
                    public void onSuccess(SendResult<Long, ItemSoldDTO> result) {
                        log.info("package with id <{}> and sold items quantity <{}> sent successfully!",id,quantity);
                    }
                    @Override
                    public void onFailure(Throwable ex) {
                        log.info(String.format("message failed to send, see stack trace below:\n%s", ex.getMessage()));
                    }
                });
            }
        }else {
            log.error("package with id <{}> does not available in SaleOrders service >>> ...", id);
            throw new CustomItemsException(id, "package not found!...");
        }
        return true;
    }
}
