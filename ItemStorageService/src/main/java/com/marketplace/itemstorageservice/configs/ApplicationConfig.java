package com.marketplace.itemstorageservice.configs;

import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.models.BrandName;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    private final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

    @Bean
    public Cache<Long, Item> ItemCache(@Value("${app.items.cache.size}") int cacheSize) {
        return cacheManager.createCache("Item-Cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Item.class,
                                ResourcePoolsBuilder.heap(cacheSize)).build());
    }

    @Bean
    public Cache<Long, BrandName> BrandCache(@Value("${app.brands.cache.size}") int cacheSize) {
        return cacheManager.createCache("Brand-Cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, BrandName.class,
                        ResourcePoolsBuilder.heap(cacheSize)).build());
    }
}