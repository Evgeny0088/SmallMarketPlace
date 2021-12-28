package com.example.itemstorageservice.Services;

import com.example.itemstorageservice.Models.Item;

import java.util.List;

public interface ItemServiceInterface {
    List<Item> allItems();
    void createNewItem(Item item);
    void updateItem(Long id,Item item);
    String itemDeleted(Long id);
}
