package com.marketplace.itemstorageservice.configs.redis;

public interface CustomSerializer{
    <T> T doDeserialization(byte[] data);
}
