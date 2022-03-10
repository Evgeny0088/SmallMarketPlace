package com.marketPlace.itemstorageservice.services;

import java.util.List;
import java.util.Optional;

public interface ItemDetailedDTOService<T,R> {
    List<T> allItems();
    T getUpdatedItemDetailedDTO(long package_id);
    int removeItemsFromPackage(R itemSoldDTO);
}
