package com.marketPlace.itemstorageservice.services;

import java.util.List;
import java.util.Optional;

public interface ItemDetailedDTOServiceInterface<T,R> {
    List<T> allItems();
    Optional<T> getUpdatedItemDetailedDTO();
    void removeItemsFromPackage(R itemSoldDTO);
}
