package com.marketplace.itemstorageservice;

import com.marketplace.itemstorageservice.controller.BrandController;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@Disabled
@WebMvcTest(BrandController.class)
public class IntegrTest {
    @Test
    @DisplayName("get all brands")
    void checkTest() {
    }
}
