package com.marketplace.itemstorageservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.marketplace.itemstorageservice.configs.ServiceTestConfig;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.services.ItemServiceImpl;
import com.smallmarketplace.RequestBodyParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.marketplace.itemstorageservice.utilFunctions.WireMockClientResponses.*;
import static com.marketplace.itemstorageservice.utilFunctions.WireMockServers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = ServiceTestConfig.Initializer.class, classes = {ServiceTestConfig.class})
class ItemControllerWireMockTest {

    private static final String URI_ITEMS = "/items";
    private static final String URI_NEW_ITEM = "/items/new";
    private static final String URI_UPDATE_ITEM = "/items/update?itemId=%s";
    private static final String URI_DELETE_ITEM = "/items/delete/%s";
    private static final String RESOURCE_FILE_BAD_REQUEST_JSON = "testResponsesItemController/ItemWrongCreation_BAD_REQUEST.json";

    @Autowired
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
        //given
        String serialized = objectMapper.writeValueAsString(itemService.allItems());
        //then
        wireMockServer_GET_With_OK_ResourceBody(itemWireMockServer, RESOURCE_FILE_BAD_REQUEST_JSON, URI_ITEMS);
        webTestClient_GET_With_OK_JsonBody(webTestClient, URI_ITEMS);
    }

    @DisplayName("create new Item with valid params in request body")
    @ParameterizedTest(name = "test case: => serial={0}, brandId={1}, parentId={2}, item_type={3}")
    @CsvSource(value = {
            "100, 1, 2, ITEM",
            "100, 1, null, PACK",})
    void newItemTest(String serial, String brandId, String parentId, String itemType){
        //given
        String returnMessage = "new Item is created!";
        String requestBody = RequestBodyParser.requestBody(serial, brandId, parentId, itemType);
        //when
        doAnswer(InvocationOnMock::getArguments).when(itemService).createNewItem(any(Item.class));
        //then
        wireMockServer_POST_With_OK_ReturnMessage(itemWireMockServer, returnMessage, URI_NEW_ITEM);
        String actual = webTestClient_POST_With_OK_ReturnMessage(webTestClient, requestBody, URI_NEW_ITEM);

        Assertions.assertTrue(actual.contains(returnMessage));
        Mockito.verify(itemService,times(1)).createNewItem(any(Item.class));
    }

    @DisplayName("""
            Item creation failed if one of the parameters in request body is invalid, it throws BAD_REQUEST.
            Custom exceptions from service layer will be captured in service tests!
            """)
    @ParameterizedTest(name = "test case: => serial={0}, brandId={1}, parentId={2}, item_type={3}")
    @CsvSource(value = {
            "100, null, 2, ITEM, BAD_REQUEST",
            "null, 1, null, ITEM, BAD_REQUEST",
            "null, 1, 2, WRONG_TYPE, BAD_REQUEST",
            "100, null, null, PACK, BAD_REQUEST",
            "100, 2, 1, NOT_VALID, BAD_REQUEST",
    })
    void newItemFailedBrandDoesNotExistTest(String serial, String brandId, String parentId, String itemType, String errorStatus){
        //when
        String requestBody = RequestBodyParser.requestBody(serial, brandId, parentId, itemType);
        //then
        wireMockServer_POST_With_BAD_REQUEST(itemWireMockServer, RESOURCE_FILE_BAD_REQUEST_JSON, URI_NEW_ITEM);
        webTestClient_POST_With_BAD_REQUEST(webTestClient,requestBody, URI_NEW_ITEM, errorStatus);
        Mockito.verify(itemService,never()).createNewItem(any(Item.class));
    }

    @DisplayName("update new Item valid params in request body")
    @ParameterizedTest(name = "test case: => itemId={0}, serial={1}, brandId={2}, parentId={3}, item_type={4}")
    @CsvSource(value = {
            "1, 100, 1, 2, ITEM",
            "1, 100, 1, null, PACK",
    })
    void updateItemTest(String itemId, String serial, String brandId, String parentId, String itemType){
        //given
        String uri = String.format(URI_UPDATE_ITEM, itemId);
        String returnMessage = String.format("item with %s successfully updated", itemId);
        String requestBody = RequestBodyParser.requestBody(serial, brandId, parentId, itemType);
        //when
        doAnswer(InvocationOnMock::getArguments).when(itemService).updateItem(anyLong(),any(Item.class));
        //then
        wireMockServer_POST_With_OK_ReturnMessage(itemWireMockServer, returnMessage, uri);
        String actual = webTestClient_POST_With_OK_ReturnMessage(webTestClient, requestBody, uri);

        Assertions.assertTrue(actual.contains(returnMessage));
        Mockito.verify(itemService,times(1)).updateItem(anyLong(), any(Item.class));
    }

    @DisplayName("Item update failed if one of the parameters in request body is invalid, it throws BAD_REQUEST")
    @ParameterizedTest(name = "test case: => itemId={0}, serial={1}, brandId={2}, parentId={3}, item_type={4}")
    @CsvSource(value = {
            "1, 100, null, 2, ITEM, BAD_REQUEST",
            "NOT_DIGIT, null, 1, null, ITEM, BAD_REQUEST",
            "NOT_DIGIT, 100, 1, 2, ITEM, BAD_REQUEST",
            "2, null, 1, 2, WRONG_TYPE, BAD_REQUEST",
            "1, 100, null, null, PACK, BAD_REQUEST",
            "4, 100, 2, 1, NOT_VALID, BAD_REQUEST"
    })
    void updateItemFailedBrandDoesNotExistTest(String itemId, String serial,
                                               String brandId, String parentId,
                                               String itemType, String errorStatus){
        //given
        String uri = String.format(URI_UPDATE_ITEM, itemId);
        String requestBody = RequestBodyParser.requestBody(serial, brandId, parentId, itemType);
        //then
        wireMockServer_POST_With_BAD_REQUEST(itemWireMockServer,RESOURCE_FILE_BAD_REQUEST_JSON, uri);
        webTestClient_POST_With_BAD_REQUEST(webTestClient,requestBody, uri, errorStatus);
        Mockito.verify(itemService,never()).createNewItem(any(Item.class));
    }

    @DisplayName("""
            delete item from DB.
            if id is found then we remove item
            if this not DIGIT, then input validation fails and throws BAD_REQUEST
                    """)
    @ParameterizedTest(name = "test case: => itemId={0}")
    @ValueSource(strings = {"1", "NOT_DIGIT"})
    void itemIsDeletedTest(String itemId){
        //given
        String uri = String.format(URI_DELETE_ITEM, itemId);
        int times = 0;
        if (itemId.matches("\\d")){
            //when id is valid digit
            String returnMessage = String.format("item with <%s> is deleted!", itemId);
            when(itemService.itemDeleted(Long.parseLong(itemId))).thenReturn(returnMessage);
            //then
            wireMockServer_GET_With_OK_ReturnMessage(itemWireMockServer, returnMessage, uri);
            webTestClient_GET_With_OK_ResponseBody(webTestClient, uri);
            times = 1;
        }else {
            //if id is NOT digit then throw BAD_REQUEST
            String errorStatus = "BAD_REQUEST";
            wireMockServer_GET_With_BAD_REQUEST(itemWireMockServer, RESOURCE_FILE_BAD_REQUEST_JSON, uri);
            webTestClient_GET_With_BAD_REQUEST(webTestClient, uri, errorStatus);
        }
        Mockito.verify(itemService, times(times)).itemDeleted(anyLong());
    }
}