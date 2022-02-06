package com.marketPlace.itemstorageservice.services;

import com.marketPlace.itemstorageservice.models.Item;

import java.util.List;

public interface ItemServiceInterface {
    List<Item> allItems();
    void createNewItem(Item item);
    void updateItem(Long id,Item item);
    String itemDeleted(Long id);
}
