package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.configs.BrandServiceTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = BrandServiceTestConfig.class)
@ActiveProfiles("test")
public class UpdateBrandServiceTest {

    @Test
    void check(){

    }
}
