package com.marketplace.itemstorageservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.marketplace.itemstorageservice.configs.BrandWireMockConfig;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.services.BrandService;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
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
        brandWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/brands"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("testResponsesBrandController/allBrands.json")));

        webTestClient.get().uri("/brands")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(objectMapper.writeValueAsString(brandService.allBrands()));
    }

    @Test
    @DisplayName("create new BrandName in DB, should be called one times")
    void newBrandTest() throws IOException {
        BrandName newBrand = new BrandName("new brand", "0.1");
        newBrand.setId(1L);
        String uri = "/brands/new";
        String requestBody = objectMapper.writeValueAsString(newBrand);
        String returnMessage = "new brand successfully added with id 1!";
        when(brandService.postNewBrandName(Mockito.any(BrandName.class))).thenReturn(newBrand);
        brandWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", ContentType.DEFAULT_TEXT.toString())
                        .withBody(returnMessage)));

        WebTestClient.BodyContentSpec response = webTestClient.post().uri(uri)
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .body(BodyInserters.fromValue(requestBody))
                .exchange().expectBody();
        Assertions.assertTrue(response.returnResult().toString().contains(returnMessage));
        Mockito.verify(brandService, times(1)).postNewBrandName(Mockito.any());
    }

    @Test
    @DisplayName("verify that brand name is not blank")
    void newBrandWithNotNullBrandNameShouldReturnExceptionTest() throws Exception {
        BrandName brandNameIsBlank = new BrandName("","");
        brandNameIsBlank.setId(1L);
        String requestBody = objectMapper.writeValueAsString(brandNameIsBlank);
        String resourceFile = "testResponsesBrandController/BrandNameIsBlankError.json";
        String uri = "/brands/new";
        brandWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile(resourceFile)));

        webTestClient.post().uri(uri)
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .body(BodyInserters.fromValue(requestBody))
                .exchange().expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("BAD_REQUEST");
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

        brandWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", ContentType.DEFAULT_TEXT.toString())
                        .withBody(returnMessage)));

        WebTestClient.BodyContentSpec response = webTestClient.post().uri(uri)
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .body(BodyInserters.fromValue(requestBody))
                .exchange().expectStatus().isOk()
                .expectBody();
        Assertions.assertTrue(response.returnResult().toString().contains(returnMessage));
        Mockito.verify(brandService, times(1)).updateBrand(any(String.class),any());
    }

    @Test
    @DisplayName("inputs are invalid and BAD_REQUEST should be returned")
    void updateBrandInvalidInputsTest() throws JsonProcessingException {
        BrandName invalidBrand = new BrandName("", "0.1");
        String brandNameFromDB = "BrandFromDB";
        String uri = String.format("/brands/update?brand_name=%s", brandNameFromDB);
        String requestBody = objectMapper.writeValueAsString(brandNameFromDB);
        String resourceFile = "testResponsesBrandController/BrandNameIsBlankError.json";
        brandWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile(resourceFile)));

        webTestClient.post().uri(uri)
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .body(BodyInserters.fromValue(requestBody))
                .exchange().expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("BAD_REQUEST");
        Mockito.verify(brandService, times(0)).updateBrand(any(String.class),any());
    }

    @Test
    @DisplayName("delete brand from DB")
    void brandIsDeletedTest(){
        Long brandId = 1L;
        String uri = String.format("/brands/delete/%d", brandId);
        String returnMessage = String.format("brand with <%d> is deleted!", brandId);
        when(brandService.brandDeleted(brandId)).thenReturn(returnMessage);
        brandWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", ContentType.DEFAULT_TEXT.toString())
                .withBody(returnMessage)));

        webTestClient.get().uri(uri)
                .exchange().expectStatus().isOk()
                .expectBody();
        Mockito.verify(brandService, times(1)).brandDeleted(anyLong());
    }
}