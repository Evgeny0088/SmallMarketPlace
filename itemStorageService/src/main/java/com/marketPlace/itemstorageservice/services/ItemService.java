package com.marketPlace.itemstorageservice.services;

import com.marketPlace.itemstorageservice.models.BrandName;
import com.marketPlace.itemstorageservice.models.Item;
import com.marketPlace.itemstorageservice.models.ItemType;
import com.marketPlace.itemstorageservice.repositories.BrandNameRepo;
import com.marketPlace.itemstorageservice.repositories.ItemRepo;
import com.marketPlace.itemstorageservice.exceptions.CustomItemsException;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemService implements ItemServiceInterface{

    @Autowired
    Cache<Long, Item> itemCache;

    @Autowired
    ItemRepo itemRepo;

    @Autowired
    BrandNameRepo brandRepo;

    @Override
    public List<Item> allItems(){
        List<Item> items = itemRepo.findAll();
        items.forEach(item->itemCache.put(item.getId(),item));
        return items;
    }

    @Override
    public void createNewItem(Item item) throws CustomItemsException{
        BrandName brand = isValidBrand(item);
        if (brand!=null){
            item.setBrandName(brand);
            item.setCreationDate(LocalDateTime.now());
            Item parent = isNotNullParent(item.getParentItem());
            if (parent!=null) {
                item.setParentItem(parent);
                if (item.getParentItem().getItem_type() == ItemType.ITEM){
                    throw new CustomItemsException(item.getItem_type().name(),
                            "parent should have PACK item type, check inputs!");
                }
                if (item.getParentItem().getBrandName() != item.getBrandName()){
                    throw new CustomItemsException(item.getItem_type().name(),
                            "item and parent should have same brand, check inputs!");
                }
                parent.getChildItems().add(item);
                itemRepo.save(parent);
            }else {
                if (item.getItem_type() == ItemType.ITEM){
                    throw new CustomItemsException(item.getItem_type().name(),
                            "item must have PACK type if parent is null, check inputs!");
                }
                item.setParentItem(null);
                itemRepo.save(item);
            }
        }else {
            throw new CustomItemsException(item.getItem_type().name(), "item not possible to create, check inputs!");
        }
    }

    @Override
    public void updateItem(Long id,Item item) throws CustomItemsException{
        Item itemDB = itemRepo.findById(id).orElse(null);
        BrandName brand = isValidBrand(item);
        Item parent = isNotNullParent(item.getParentItem());
        if (itemDB!=null && brand!=null){
            itemDB.setBrandName(brand);
            itemDB.setSerial(item.getSerial());
            itemDB.setItem_type(item.getItem_type());
            itemDB.setCreationDate(LocalDateTime.now());
            if (parent==null && itemDB.getParentItem()!=null){
                itemDB.getParentItem().getChildItems().remove(itemDB);
                itemDB.setParentItem(null);
            }else {
                if (parent != null){
                    if (parent.getItem_type() == ItemType.ITEM){
                        throw new CustomItemsException(id,
                                "parent should have PACK item type, check inputs!");
                    }
                    if (!parent.getBrandName().getName().equals(itemDB.getBrandName().getName())){
                        throw new CustomItemsException(id,
                                "brand in parent item should be the same as child item, check inputs!");
                    }
                    itemDB.setParentItem(parent);
                }
            }
            itemRepo.save(itemDB);
            itemCache.put(itemDB.getId(), itemDB);
        }else {
            throw new CustomItemsException(id,"item not possible to update, " + "check inputs!");
        }
    }

    @Override
    public String itemDeleted(Long id) throws CustomItemsException{
        Item itemToRemove = itemRepo.findById(id).orElse(null);
        if (itemToRemove != null){
            String result = removeSoldItemsFromDB(itemToRemove);
            return String.format(result, id);
        }else {
            throw new CustomItemsException("item with id: " + id, "is not found");
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

    private String removeSoldItemsFromDB(Item item){
        if (item==null){
            return "item with %d is deleted!";
        }
        Item parent = item.getParentItem();
        Long itemToRemove = item.getId();
        itemRepo.deleteById(itemToRemove);
        itemCache.remove(itemToRemove);
        return parent != null && parent.getChildItems().isEmpty() ? removeSoldItemsFromDB(parent) : removeSoldItemsFromDB(null);
    }
}
