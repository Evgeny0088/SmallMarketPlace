package com.marketplace.itemstorageservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.marketplace.itemstorageservice.configs.ItemWireMockConfig;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.services.ItemServiceImpl;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
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

import static com.marketplace.itemstorageservice.utilFunctions.HelperFunctions.requestBody;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@ContextConfiguration(classes = {ItemWireMockConfig.class})
class WireMockItemControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(WireMockItemControllerTest.class);

    @Autowired
    @Qualifier("itemWireMock")
    WireMockServer itemWireMockServer;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    ItemServiceImpl itemService;

    @BeforeEach
    void clearWireMock() {
        itemWireMockServer.resetAll();
    }

    @Test
    @DisplayName("get all items from database")
    void allItemsTest() throws JsonProcessingException {
        String uri = "/items";
        itemWireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("testResponsesItemController/allItems.json")));

        webTestClient.get().uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(objectMapper.writeValueAsString(itemService.allItems()));
    }

    @DisplayName("create new Item valid params in request body")
    @ParameterizedTest(name = "test case: => serial={0}, brandId={1}, parentId={2}, item_type={3}")
    @CsvSource(value = {
            "100, 1, 2, ITEM",
            "100, 1, null, PACK",})
    void newItemTest(String serial, String brandId, String parentId, String itemType){
        String uri = "/items/new";
        String returnMessage = "new Item is created!";
        String requestBody = requestBody(serial, brandId, parentId, itemType);

        doAnswer(InvocationOnMock::getArguments).when(itemService).createNewItem(any(Item.class));

        itemWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(uri))
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
        Mockito.verify(itemService,times(1)).createNewItem(any(Item.class));
    }

    @DisplayName("Item creation failed if one of the parameters in request body is invalid")
    @ParameterizedTest(name = "test case: => serial={0}, brandId={1}, parentId={2}, item_type={3}")
    @CsvSource(value = {
            "100, null, 2, ITEM, BAD_REQUEST",
            "null, 1, null, ITEM, BAD_REQUEST",
            "null, 1, 2, WRONG_TYPE, BAD_REQUEST",
            "100, null, null, PACK, BAD_REQUEST",
            "100, 2, 1, NOT_VALID, BAD_REQUEST"
    })
    void newItemFailedBrandDoesNotExistTest(String serial, String brandId, String parentId, String itemType, String errorStatus){
        String uri = "/items/new";
        String resourceFile = "testResponsesItemController/ItemWrongCreationDueToBrand.json";
        String requestBody = requestBody(serial, brandId, parentId, itemType);
        wireMockServerWithBAD_REQUEST(resourceFile, uri);
        webTestClientWithBAD_REQUEST(requestBody, uri, errorStatus);

        Mockito.verify(itemService,never()).createNewItem(Mockito.any(Item.class));
    }

    private void wireMockServerWithBAD_REQUEST(String resourceFile, String uri){
        itemWireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo(uri))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(resourceFile)));
    }

    private void webTestClientWithBAD_REQUEST(String requestBody, String uri, String errorStatus){
        webTestClient.post().uri(uri)
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .body(BodyInserters.fromValue(requestBody))
                .exchange().expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(errorStatus);
    }
}