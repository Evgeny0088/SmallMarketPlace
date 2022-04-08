package com.marketplace.itemstorageservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.marketplace.itemstorageservice.configs.WireMockConfig;
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
@ContextConfiguration(classes = {WireMockConfig.class})
class BrandControllerWireMockTest {

    private static final Logger logger = LoggerFactory.getLogger(BrandControllerWireMockTest.class);
    private static final String URI_BRANDS = "/brands";
    private static final String URI_NEW_BRAND = "/brands/new";
    private static final String URI_UPDATE_BRAND = "/brands/update?brand_name=%s";
    private static final String URI_DELETE_BRAND = "/brands/delete/%s";
    private static final String RESOURCE_FILE_ALL_BRANDS_JSON = "testResponsesBrandController/allBrands.json";
    private static final String RESOURCE_FILE_BAD_REQUEST_JSON = "testResponsesItemController/ItemWrongCreation_BAD_REQUEST.json";

    @Autowired
    WireMockServer brandWireMockServer;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    BrandService brandService;

    @Test
    @DisplayName("connection test whether wiremock is running or not")
    void wireMockHealthCheck() {
        brandWireMockServer.isRunning();
        logger.info("wire mock is running!>>>>>>>>>");
    }

    @Test
    @DisplayName("get all brands from database")
    void allBrandsTest() throws JsonProcessingException {
        //when
        String serialized = objectMapper.writeValueAsString(brandService.allBrands());
        wireMockServer_GET_With_OK_ResourceBody(brandWireMockServer,RESOURCE_FILE_ALL_BRANDS_JSON, URI_BRANDS);
        //then
        webTestClient_GET_With_OK_JsonBody(webTestClient, URI_BRANDS, serialized);
    }

    @Test
    @DisplayName("create new BrandName")
    void newBrandTest() throws IOException {
        //given
        BrandName newBrand = new BrandName("new brand", "0.1");
        newBrand.setId(1L);
        String requestBody = objectMapper.writeValueAsString(newBrand);
        String returnMessage = "new brand successfully added with id 1!";
        //when
        when(brandService.postNewBrandName(Mockito.any(BrandName.class))).thenReturn(newBrand);
        wireMockServer_POST_With_OK_ReturnMessage(brandWireMockServer, returnMessage, URI_NEW_BRAND);
        String actual = webTestClient_POST_With_OK_ReturnMessage(webTestClient, requestBody, URI_NEW_BRAND);
        //then
        Assertions.assertTrue(actual.contains(returnMessage));
        Mockito.verify(brandService, times(1)).postNewBrandName(Mockito.any());
    }

    @Test
    @DisplayName("failed to create new brand if brand name is blank")
    void newBrandWithNotNullBrandNameShouldReturnExceptionTest() throws Exception {
        //given
        BrandName brandNameIsBlank = new BrandName("","");
        brandNameIsBlank.setId(1L);
        String requestBody = objectMapper.writeValueAsString(brandNameIsBlank);
        String errorStatus = "BAD_REQUEST";

        //when and then:)
        wireMockServer_POST_With_BAD_REQUEST(brandWireMockServer, RESOURCE_FILE_BAD_REQUEST_JSON,URI_NEW_BRAND);
        webTestClient_POST_With_BAD_REQUEST(webTestClient, requestBody, URI_NEW_BRAND, errorStatus);

        Mockito.verify(brandService, times(0)).postNewBrandName(Mockito.any());
    }

    @Test
    @DisplayName("update BrandName")
    void updateBrandNameTest() throws JsonProcessingException {
        //given
        BrandName updatedBrand = new BrandName("gucci", "0.1");
        updatedBrand.setId(1L);
        String brandNameFromDB = "updatable brand";
        String requestBody = objectMapper.writeValueAsString(updatedBrand);
        String uri = String.format(URI_UPDATE_BRAND, brandNameFromDB);
        String returnMessage = String.format("brand with name %s successfully updated",brandNameFromDB);

        //when
        doNothing().when(brandService).updateBrand(any(String.class),any(BrandName.class));
        wireMockServer_POST_With_OK_ReturnMessage(brandWireMockServer, returnMessage, uri);
        String actual = webTestClient_POST_With_OK_ReturnMessage(webTestClient, requestBody, uri);

        //then
        Assertions.assertTrue(actual.contains(returnMessage));
        Mockito.verify(brandService, times(1)).updateBrand(any(String.class),any());
    }

    @Test
    @DisplayName("inputs are invalid should throw BAD_REQUEST")
    void updateBrandInvalidInputsTest() throws JsonProcessingException {
        //given
        String brandNameFromDB = "BrandFromDB";
        String uri = String.format(URI_UPDATE_BRAND, brandNameFromDB);
        String requestBody = objectMapper.writeValueAsString(brandNameFromDB);
        String errorStatus = "BAD_REQUEST";
        //when
        wireMockServer_POST_With_BAD_REQUEST(brandWireMockServer, RESOURCE_FILE_BAD_REQUEST_JSON, uri);
        webTestClient_POST_With_BAD_REQUEST(webTestClient, requestBody, uri, errorStatus);
        //then
        Mockito.verify(brandService, times(0)).updateBrand(any(String.class),any());
    }

    @DisplayName("delete brand from DB")
    @ParameterizedTest(name = "test case: => brandId={0}")
    @ValueSource(strings = {"1", "NOT_DIGIT"})
    void brandIsDeletedTest(String brandId){
        //given
        String uri = String.format(URI_DELETE_BRAND, brandId);
        int times = 0;
        //when
        if (brandId.matches("\\d")){
            String returnMessage = String.format("brand with <%s> is deleted!", brandId);
            when(brandService.brandDeleted(Long.parseLong(brandId))).thenReturn(returnMessage);
            wireMockServer_GET_With_OK_ReturnMessage(brandWireMockServer, returnMessage, uri);
            webTestClient_GET_With_OK_ResponseBody(webTestClient, uri);
            times = 1;
        }else {
            String errorStatus = "BAD_REQUEST";
            wireMockServer_GET_With_BAD_REQUEST(brandWireMockServer, RESOURCE_FILE_BAD_REQUEST_JSON, uri);
            webTestClient_GET_With_BAD_REQUEST(webTestClient, uri, errorStatus);
        }
        //then
        Mockito.verify(brandService, times(times)).brandDeleted(anyLong());
    }
}