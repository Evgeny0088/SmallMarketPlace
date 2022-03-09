package com.marketPlace.Orders.service;

import com.marketPlace.Orders.DTOModels.ItemDetailedInfoDTO;
import com.marketPlace.Orders.DTOModels.ItemSoldDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class ItemProducerServiceImpl implements ItemProducerService {

    private final NewTopic itemSold;
    private final KafkaTemplate<String, ItemSoldDTO> itemSoldTemplate;
    private final Cache<Long, ItemDetailedInfoDTO> itemDetailedInfoDTOCache;

    @Autowired
    public ItemProducerServiceImpl(NewTopic itemSold, KafkaTemplate<String, ItemSoldDTO> itemSoldTemplate, Cache<Long, ItemDetailedInfoDTO> itemDetailedInfoDTOCache) {
        this.itemSold = itemSold;
        this.itemSoldTemplate = itemSoldTemplate;
        this.itemDetailedInfoDTOCache = itemDetailedInfoDTOCache;
    }

    @Override
    public boolean sendSoldItem(long id, int quantity) {
        if (itemDetailedInfoDTOCache.containsKey(id)){
            ItemDetailedInfoDTO packageOfSoldItems = itemDetailedInfoDTOCache.get(id);
            long currentQuantity = packageOfSoldItems.getItemsQuantityInPack()-quantity;
            packageOfSoldItems.setItemsQuantityInPack(currentQuantity<0 ? packageOfSoldItems.getItemsQuantityInPack() : currentQuantity);
            ItemSoldDTO soldItems;
            if (currentQuantity<0){
                soldItems = new ItemSoldDTO(id,(int)currentQuantity);
                log.warn("not enough items in current package: <{}>, need to check in DB...",currentQuantity);
            }else {
                soldItems = new ItemSoldDTO(id,quantity);
            }
            ListenableFuture<SendResult<String, ItemSoldDTO>> future = itemSoldTemplate.send(itemSold.name(), soldItems);
            future.addCallback(new ListenableFutureCallback<>(){
                @Override
                public void onSuccess(SendResult<String, ItemSoldDTO> result) {
                    log.info("package with id <{}> and sold items quantity <{}> sent successfully!",id,quantity);
                }
                @Override
                public void onFailure(Throwable ex) {
                    log.info(String.format("message failed to send, see stack trace below:\n%s", ex.getMessage()));
                }
            });
        }else {
            log.warn("package with id <{}> does not available in cache >>> ...", id);
            return false;
        }
        return true;
    }
}
