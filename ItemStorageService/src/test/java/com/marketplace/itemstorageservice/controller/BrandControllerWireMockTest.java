package com.marketplace.itemstorageservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.services.BrandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;

import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@ContextConfiguration(classes = {WireMockConfig.class})
class BrandControllerWireMockTest {

    private static final Logger logger = LoggerFactory.getLogger(BrandControllerWireMockTest.class);

    @Autowired
    WireMockServer brandWireMockServer;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    BrandService brandService;

    @BeforeEach
    void clearWireMock() {
        brandWireMockServer.resetAll();
    }

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
                        .withBodyFile("wireMockResponces/allBrands.json")));
        webTestClient.get().uri("/brands")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(objectMapper.writeValueAsString(brandService.allBrands()));
    }

    @Test
    @DisplayName("create new BrandName in DB, should be called one times")
    void newBrandTest() {
        BrandName newBrand = new BrandName("new brand", "0.1");
        newBrand.setId(1L);
        String returnMessage = String.format("new brand successfully added with id %d!", newBrand.getId());
        brandWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/brands/new"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/x-www-form-urlencoded")
                        .withBody(returnMessage)));
        webTestClient.post().uri("/brands/new")
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody().equals(returnMessage);
        Mockito.verify(brandService, times(1)).postNewBrandName(Mockito.any(BrandName.class));

    }

    @Test
    @DisplayName("verify if brand name is not blank")
    void newBrandWithNotNullBrandNameShouldReturnExceptionTest() throws Exception {
        BrandName nullBrandName = new BrandName("","");
        String url = "/new";
        brandWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(url))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.BAD_REQUEST.value())));
        Mockito.verify(brandService, times(0)).postNewBrandName(Mockito.any());
    }

}