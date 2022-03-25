package com.marketplace.orders.service;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

public interface ItemConsumerService<K, T> {
    void receiveAllPackagesFromItemStorage(List<T> itemList);
    T updateItemDetailedDTOInCache(List<T> item);
    void redisTransactionalAction(List<T> itemList, RedisTemplate<K, T> template);
}
