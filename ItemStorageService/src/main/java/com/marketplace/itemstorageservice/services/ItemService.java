package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.models.Item;

import java.util.List;

public interface ItemService {
    List<Item> allItems();
    void createNewItem(Item item);
    void updateItem(Long id,Item item);
    String itemDeleted(Long id);
}
