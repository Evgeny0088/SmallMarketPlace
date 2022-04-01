package com.marketplace.itemstorageservice.controller;

import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.services.BrandServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@WebMvcTest(BrandController.class)
class BrandControllerTest_BACKUP {

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

    @Test
    @DisplayName("create new BrandName in DB, should be called one times")
    void newBrandTest() throws Exception {
        BrandName newBrand = new BrandName("gucci", "0.1");
        when(brandService.postNewBrandName(Mockito.any(BrandName.class))).thenReturn(newBrand);
        String url = "/newBrand";
        mockMvc.perform(post(url)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(newBrand)))
                .andExpect(status().isOk())
                .andDo(print());
        Mockito.verify(brandService, times(1)).postNewBrandName(Mockito.any());
    }

    @Test
    @DisplayName("verify if brand name is not blank")
    void newBrandWithNotNullBrandNameShouldReturnExceptionTest() throws Exception {
        BrandName nullBrandName = new BrandName("","");
        String url = "/newBrand";
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(nullBrandName)))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
        Mockito.verify(brandService, times(0)).postNewBrandName(Mockito.any());
    }

    @Test
    @DisplayName("update BrandName")
    void updateBrandTest() throws Exception {
        BrandName updatedBrand = new BrandName("gucci", "0.1");
        String BrandNameFromDB = "AnyBrandFromDB";
        doNothing().when(brandService).updateBrand(any(String.class),any(BrandName.class));
        brandService.updateBrand(BrandNameFromDB,updatedBrand);
        String url = String.format("/brand/update?brand_name=%s", BrandNameFromDB);
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedBrand)))
                        .andExpect(status().isOk())
                        .andDo(print());
        Mockito.verify(brandService, times(1)).updateBrand(BrandNameFromDB,updatedBrand);
    }

    @Test
    @DisplayName("inputs are invalid and bad request will return")
    void updateBrandInvalidTest() throws Exception {
        BrandName invalidBrand = new BrandName("", "0.1");
        String BrandNameFromDB = "BrandFromDB";
        String url = String.format("/brand/update?brand_name=%s", BrandNameFromDB);
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidBrand)))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
        Mockito.verify(brandService, times(0)).updateBrand(BrandNameFromDB,invalidBrand);
    }

    @Test
    @DisplayName("delete brand from DB")
    void brandIsDeletedTest() throws Exception {
        Long brandId = 1L;
        when(brandService.brandDeleted(brandId)).thenReturn(anyString());
        String url = String.format("/brand/delete/%d", brandId);
        mockMvc.perform(get(url)
                        .contentType("text/plain;charset=UTF-8"))
                        .andDo(print())
                        .andExpect(status().isOk()).andReturn();
    }
}