package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.exceptions.CustomItemsException;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.models.ItemType;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import com.marketplace.itemstorageservice.repositories.ItemRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepo itemRepo;
    private final BrandNameRepo brandRepo;
    private final Cache<Long, Item> itemCache;
    private final KafkaTemplate<String, List<ItemDetailedInfoDTO>> ItemDetailedDTOUpdate;
    private final NewTopic updateItemTopic;

    @Autowired
    public ItemServiceImpl(Cache<Long, Item> itemCache,
                           ItemRepo itemRepo,
                           BrandNameRepo brandRepo,
                           KafkaTemplate<String, List<ItemDetailedInfoDTO>> ItemDetailedDTOUpdate,
                           @Qualifier("updateItemRequest") NewTopic updateItemTopic) {
        this.itemCache = itemCache;
        this.itemRepo = itemRepo;
        this.brandRepo = brandRepo;
        this.ItemDetailedDTOUpdate = ItemDetailedDTOUpdate;
        this.updateItemTopic = updateItemTopic;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> allItems(){
        List<Item> items = itemRepo.findAll();
        items.forEach(item->itemCache.put(item.getId(),item));
        return items;
    }

    @Override
    @Transactional(rollbackFor = CustomItemsException.class)
    public void createNewItem(Item item) throws CustomItemsException{
        BrandName brand = isValidBrand(item);
        String errorMessage;
        if (brand!=null){
            item.setBrandName(brand);
            item.setCreationDate(LocalDateTime.now());
            Item parent = isNotNullParent(item.getParentItem());
            if (parent!=null) {
                item.setParentItem(parent);
                if (item.getParentItem().getItem_type() == ItemType.ITEM){
                    errorMessage = String.format("parent <%s> should have PACK item type, check inputs!",item);
                    log.error(errorMessage);
                    throw new CustomItemsException(item.getItem_type().name(), "should have PACK item type, check inputs!");
                }
                if (item.getParentItem().getBrandName() != item.getBrandName()){
                    errorMessage = "item and parent should have same brand, check inputs!";
                    log.error(errorMessage);
                    throw new CustomItemsException(item.getItem_type().name(), errorMessage);
                }
                parent.getChildItems().add(item);
                itemRepo.save(parent);
                itemCache.put(parent.getId(), parent);
                sendRequestForPackageUpdate(parent);
            }else {
                if (item.getItem_type() == ItemType.ITEM){
                    errorMessage = "item must have PACK type if parent is null, check inputs!";
                    log.error(errorMessage);
                    throw new CustomItemsException(item.getItem_type().name(),
                            "item must have PACK type if parent is null, check inputs!");
                }
                item.setParentItem(null);
                itemRepo.save(item);
                itemCache.put(item.getId(),item);
            }
        }else {
            errorMessage = "item not possible to create, check inputs!";
            log.error(errorMessage);
            throw new CustomItemsException(item.getItem_type().name(), errorMessage);
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = CustomItemsException.class)
    public void updateItem(Long id,Item item) throws CustomItemsException{
        Item itemDB = itemRepo.findById(id).orElse(null);
        BrandName brand = isValidBrand(item);
        Item parent = isNotNullParent(item.getParentItem());
        String errorMessage;
        if (itemDB!=null && brand!=null){
            itemDB.setBrandName(brand);
            itemDB.setSerial(item.getSerial());
            itemDB.setItem_type(item.getItem_type());
            itemDB.setCreationDate(LocalDateTime.now());
            Item parentDB = itemDB.getParentItem();
            if (parent==null && parentDB!=null){
                if (itemDB.getItem_type() == ItemType.ITEM){
                    errorMessage = String.format("item <%s> should have PACK item type if do not have parent, check inputs!",itemDB.getId());
                    log.error(errorMessage);
                    throw new CustomItemsException(itemDB.getId(), "should have PACK item type if do not have parent, check inputs!");
                }
                parentDB.getChildItems().remove(itemDB);
                itemDB.setParentItem(null);
                itemRepo.save(parentDB);
                itemCache.put(parentDB.getId(), parentDB);
                sendRequestForPackageUpdate(parentDB);
            }else {
                if (parent != null){
                    if (parent.getItem_type() == ItemType.ITEM){
                        errorMessage = String.format("%s should have PACK item type if do not have parent, check inputs!",parent.getBrandName());
                        log.error(errorMessage);
                        throw new CustomItemsException(parent.getBrandName(), "should have PACK item type, check inputs!");
                    }
                    if (!parent.getBrandName().getName().equals(itemDB.getBrandName().getName())){
                        errorMessage = "brand in parent item should be the same as child item, check inputs!";
                        log.error(errorMessage);
                        throw new CustomItemsException(parent.getBrandName(), errorMessage);
                    }
                    itemDB.setParentItem(parent);
                    parent.getChildItems().add(itemDB);
                    itemRepo.save(parent);
                    itemCache.put(parent.getId(), parent);
                }else {
                    itemRepo.save(itemDB);
                    itemCache.put(itemDB.getId(), itemDB);
                }
                sendRequestForPackageUpdate(parent);
            }
        }else {
            errorMessage = "item not possible to update, check inputs!!";
            log.error(errorMessage);
            throw new CustomItemsException(id,errorMessage);
        }
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String itemDeleted(Long id) throws CustomItemsException{
        Item itemToRemove = itemRepo.findById(id).orElse(null);
        if (itemToRemove != null){
            String result = removeSoldItemsFromDB(itemToRemove);
            return String.format(result, id);
        }else {
            String errorMessage = "item with id <%d> is not found...";
            log.error(errorMessage);
            throw new CustomItemsException(id, "is not found");
        }
    }

    private String removeSoldItemsFromDB(Item item){
        if (item==null){
            return "item with %d is deleted!";
        }
        Item parent = item.getParentItem();
        Long itemToRemoveId = item.getId();
        itemRepo.deleteById(itemToRemoveId);
        itemCache.remove(itemToRemoveId);
        if (parent != null && parent.getChildItems().isEmpty()){
            sendRequestForPackageUpdate(parent);
            return removeSoldItemsFromDB(parent);
        }else {
            if (parent != null){
                parent.getChildItems().remove(item);
            }
            sendRequestForPackageUpdate(parent);
            return removeSoldItemsFromDB(null);
        }
    }

    private void sendRequestForPackageUpdate(Item parent) {
        long parent_id = parent != null ? parent.getId() : -1L;
        if (parent_id != -1L){
            List<ItemDetailedInfoDTO> updatedPackage = Collections.singletonList(itemRepo.getItemDTOByParentId(parent.getId()));
            ListenableFuture<SendResult<String, List<ItemDetailedInfoDTO>>> future = ItemDetailedDTOUpdate.send(updateItemTopic.name(),updatedPackage);
            future.addCallback(new ListenableFutureCallback<>(){
                @Override
                public void onSuccess(SendResult<String, List<ItemDetailedInfoDTO>> result) {
                    log.info("package with id <{}> sent to SaleOrders service!",parent.getId());
                }
                @Override
                public void onFailure(Throwable ex) {
                    log.error(String.format("message failed to send, see stack trace below:\n%s", ex.getMessage()));
                }
            });
        }else {
            log.info("there is no updates on packages, nothing to send!...");
        }
    }

    private BrandName isValidBrand(Item item) {
        return (item.getBrandName()!=null && brandRepo.findById(item.getBrandName().getId()).isPresent())
                ? brandRepo.getById(item.getBrandName().getId()) : null;
    }

    private Item isNotNullParent(Item parent){
        if (parent==null)return null;
        return itemRepo.findById(parent.getId()).orElse(null);
    }
}