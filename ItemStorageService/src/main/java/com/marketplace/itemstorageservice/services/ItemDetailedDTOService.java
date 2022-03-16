package com.marketplace.itemstorageservice.services;

import java.util.List;

public interface ItemDetailedDTOService<T,R> {
    List<T> allItems();
    T getUpdatedItemDetailedDTO(long package_id);
    int removeItemsFromPackage(R itemSoldDTO);
}
