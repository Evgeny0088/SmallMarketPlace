package com.marketplace.itemstorageservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.itemstorageservice.controller.BrandController;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.services.BrandServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrandController.class)
public class IntegrTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BrandServiceImpl brandService;

    @Test
    @DisplayName("get all brands")
    void allBrandsTest() throws Exception {
        //when
        List<BrandName> brands = new ArrayList<>();
        brands.add(new BrandName("ee","1"));
        brands.add(new BrandName("ek","1"));
        brands.add(new BrandName("em","1"));
        when(brandService.allBrands()).thenReturn(brands);
        String url = "/brands";
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk()).andDo(print())
                .andReturn();
        String actualJsonResult = result.getResponse().getContentAsString();
        String expectedJsonString = objectMapper.writeValueAsString(brands);
        assertThat(actualJsonResult).isEqualToIgnoringWhitespace(expectedJsonString);
    }
}
