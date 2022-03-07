package com.marketPlace.Orders.configs;

import com.marketPlace.Orders.DTOModels.ItemDetailedInfoDTO;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class ApplicationConfig {

    private final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

    @Bean
    public Cache<Long, ItemDetailedInfoDTO> ItemDetailedInfoDTOCache(@Value("${app.cache.size}") int cacheSize) {
        return cacheManager.createCache("ItemDTO-Cache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, ItemDetailedInfoDTO.class,
                                ResourcePoolsBuilder.heap(cacheSize)).build());
    }
}
