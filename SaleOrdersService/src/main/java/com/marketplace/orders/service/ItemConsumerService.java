package com.marketplace.orders.service;

import java.util.List;

public interface ItemConsumerService<T> {
    void receiveAllPackagesFromItemStorage(List<T> itemList);
    T updateItemDetailedDTOInCache(List<T> item);
}
