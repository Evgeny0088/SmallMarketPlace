package com.marketplace.itemstorageservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.marketplace.itemstorageservice.configs.BrandWireMockConfig;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.services.BrandService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

import static com.marketplace.itemstorageservice.utilFunctions.WireMocks.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("wiremock-test")
@ContextConfiguration(classes = {BrandWireMockConfig.class})
class BrandControllerWireMockTest {

    private static final Logger logger = LoggerFactory.getLogger(BrandControllerWireMockTest.class);

    @Autowired
    @Qualifier("brandWireMock")
    WireMockServer brandWireMockServer;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    BrandService brandService;

    @Test
    @DisplayName("connection test if whether wiremock is running")
    void wireMockHealthCheck() {
        logger.info("wire mock is running!>>>>>>>>>");
        brandWireMockServer.isRunning();
    }

    @Test
    @DisplayName("get all brands from database")
    void allBrandsTest() throws JsonProcessingException {
        String resourceBody = "testResponsesBrandController/allBrands.json";
        String uri = "/brands";
        String serialized = objectMapper.writeValueAsString(brandService.allBrands());
        wireMockServer_GET_With_OK_ResourceBody(brandWireMockServer,resourceBody, uri);
        webTestClient_GET_With_OK_JsonBody(webTestClient, uri, serialized);
    }

    @Test
    @DisplayName("create new BrandName")
    void newBrandTest() throws IOException {
        BrandName newBrand = new BrandName("new brand", "0.1");
        newBrand.setId(1L);
        String uri = "/brands/new";
        String requestBody = objectMapper.writeValueAsString(newBrand);
        String returnMessage = "new brand successfully added with id 1!";
        when(brandService.postNewBrandName(Mockito.any(BrandName.class))).thenReturn(newBrand);

        wireMockServer_POST_With_OK(brandWireMockServer, returnMessage, uri);

        String actual = webTestClient_POST_With_OK_ReturnMessage(webTestClient, requestBody, uri);

        Assertions.assertTrue(actual.contains(returnMessage));
        Mockito.verify(brandService, times(1)).postNewBrandName(Mockito.any());
    }

    @Test
    @DisplayName("failed to create new brand if brand name is blank")
    void newBrandWithNotNullBrandNameShouldReturnExceptionTest() throws Exception {
        BrandName brandNameIsBlank = new BrandName("","");
        brandNameIsBlank.setId(1L);
        String requestBody = objectMapper.writeValueAsString(brandNameIsBlank);
        String resourceFile = "testResponsesBrandController/BrandName_BAD_REQUEST.json";
        String uri = "/brands/new";
        String errorStatus = "BAD_REQUEST";
        wireMockServer_POST_With_BAD_REQUEST(brandWireMockServer, resourceFile,uri);

        webTestClient_POST_With_BAD_REQUEST(webTestClient, requestBody, uri, errorStatus);

        Mockito.verify(brandService, times(0)).postNewBrandName(Mockito.any());
    }

    @Test
    @DisplayName("update BrandName")
    void updateBrandNameTest() throws JsonProcessingException {
        BrandName updatedBrand = new BrandName("gucci", "0.1");
        updatedBrand.setId(1L);
        String brandNameFromDB = "<updatable brand>";
        String requestBody = objectMapper.writeValueAsString(updatedBrand);
        String uri = String.format("/brands/update?brand_name=%s", brandNameFromDB);
        String returnMessage = String.format("brand with name %s successfully updated",brandNameFromDB);
        doNothing().when(brandService).updateBrand(any(String.class),any(BrandName.class));

        wireMockServer_POST_With_OK(brandWireMockServer, returnMessage, uri);
        String actual = webTestClient_POST_With_OK_ReturnMessage(webTestClient, requestBody, uri);

        Assertions.assertTrue(actual.contains(returnMessage));
        Mockito.verify(brandService, times(1)).updateBrand(any(String.class),any());
    }

    @Test
    @DisplayName("inputs are invalid should throws BAD_REQUEST")
    void updateBrandInvalidInputsTest() throws JsonProcessingException {
        String brandNameFromDB = "BrandFromDB";
        String uri = String.format("/brands/update?brand_name=%s", brandNameFromDB);
        String requestBody = objectMapper.writeValueAsString(brandNameFromDB);
        String resourceFile = "testResponsesBrandController/BrandName_BAD_REQUEST.json";
        String errorStatus = "BAD_REQUEST";

        wireMockServer_POST_With_BAD_REQUEST(brandWireMockServer, resourceFile, uri);

        webTestClient_POST_With_BAD_REQUEST(webTestClient, requestBody, uri, errorStatus);

        Mockito.verify(brandService, times(0)).updateBrand(any(String.class),any());
    }

    @DisplayName("delete brand from DB")
    @ParameterizedTest(name = "test case: => brandId={0}")
    @ValueSource(strings = {"1", "NOT_DIGIT"})
    void brandIsDeletedTest(String brandId){
        String uri = String.format("/brands/delete/%s", brandId);
        int times = 0;
        if (brandId.matches("\\d")){
            String returnMessage = String.format("brand with <%s> is deleted!", brandId);
            when(brandService.brandDeleted(Long.parseLong(brandId))).thenReturn(returnMessage);
            wireMockServer_GET_With_OK_ReturnMessage(brandWireMockServer, returnMessage, uri);
            webTestClient_GET_With_OK_ResponseBody(webTestClient, uri);
            times = 1;
        }else {
            String resourceFile = "testResponsesBrandController/BrandName_BAD_REQUEST.json";
            String errorStatus = "BAD_REQUEST";
            wireMockServer_GET_With_BAD_REQUEST(brandWireMockServer, resourceFile, uri);
            webTestClient_GET_With_BAD_REQUEST(webTestClient, uri, errorStatus);
        }
        Mockito.verify(brandService, times(times)).brandDeleted(anyLong());
    }
}