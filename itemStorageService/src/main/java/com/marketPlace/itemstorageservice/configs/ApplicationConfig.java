package com.marketPlace.itemstorageservice.configs;

import com.marketPlace.itemstorageservice.models.BrandName;
import com.marketPlace.itemstorageservice.models.Item;
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
