package com.marketPlace.Orders.service;

import java.util.List;

public interface ItemConsumerService<T> {
    void receiveAllPackagesFromItemStorage(List<T> itemList);
    T updateItemDetailedDTOInCache(T item);
}
