package com.marketplace.itemstorageservice.controller;

import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.models.ItemType;
import com.marketplace.itemstorageservice.services.ItemServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@WebMvcTest(ItemController.class)
class ItemControllerTest_BACKUP {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemServiceImpl itemService;

    @Test
    @DisplayName("get all items")
    void allItemsTest() throws Exception {
        //when
        List<Item> items = new ArrayList<>();
        BrandName brand = new BrandName("brand", "0.1");
        Item parent = new Item(10L,brand,null, ItemType.PACK);
        parent.setId(100L);
        items.add(parent);
        items.add(new Item(1L,brand,parent, ItemType.ITEM));
        items.add(new Item(2L,brand,parent, ItemType.ITEM));
        items.add(new Item(3L,brand,parent, ItemType.ITEM));

        when(itemService.allItems()).thenReturn(items);

        String url = "/items";
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk()).andDo(print())
                .andReturn();
        String actualJsonResult = result.getResponse().getContentAsString();
        String expectedJsonString = objectMapper.writeValueAsString(items);
        assertThat(actualJsonResult).isEqualToIgnoringWhitespace(expectedJsonString);
    }

    @Test
    @DisplayName("create new Item with Item where parent is null and type is PACK")
    void newItemTest() throws Exception {
        String requestJson = """
                { \s
                    "serial": 100,
                    "brandName": {
                        "id":1
                                },
                    "parentItem": {
                                    "id": 32
                                    },
                    "item_type": "ITEM"
                    }
                """;
        doAnswer(InvocationOnMock::getArguments).when(itemService).createNewItem(Mockito.any(Item.class));
        String url = "/newItem";
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isOk())
                .andDo(print());
        Mockito.verify(itemService, times(1)).createNewItem(Mockito.any(Item.class));
    }

    @Test
    @DisplayName("create failed when brand is null")
    void newItemFailedBrandDoesNotExistTest() throws Exception {
        BrandName brand = new BrandName(null, "0.1");
        Item item = new Item(10L,brand,null,ItemType.PACK);
        doNothing().when(itemService).createNewItem(Mockito.any(Item.class));
        String url = "/newItem";
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andDo(print());
        Mockito.verify(itemService,never()).createNewItem(Mockito.any(Item.class));
    }

    @Test
    @DisplayName("create failed when serial is null")
    void newItemFailedSerialIsNullTest() throws Exception {
        BrandName brand = new BrandName("brand", "0.1");
        Item item = new Item(null, brand,null,ItemType.PACK);
        doNothing().when(itemService).createNewItem(Mockito.any(Item.class));
        String url = "/newItem";
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andDo(print());
        Mockito.verify(itemService,never()).createNewItem(Mockito.any(Item.class));
    }

    @Test
    @DisplayName("create failed when ItemType is null")
    void newItemFailedItemTypeIsNullTest() throws Exception {
        BrandName brand = new BrandName("brand", "0.1");
        Item item = new Item(100L, brand,null,null);
        doNothing().when(itemService).createNewItem(Mockito.any(Item.class));
        String url = "/newItem";
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest())
                .andDo(print());
        Mockito.verify(itemService,never()).createNewItem(Mockito.any(Item.class));
    }

    @Test
    @DisplayName("update item")
    void updateItemTest() throws Exception {
        BrandName brand = new BrandName();
        brand.setId(1L);
        Item parent = new Item(1L,brand,null,ItemType.PACK);
        parent.setId(1L);
        Item item = new Item(100L, brand, parent, ItemType.ITEM);
        Long id = 2L;
        item.setId(id);
        String requestJson = String.format("""
                    { \s
                        "serial": %d,
                        "brandName": {
                            "id":%d
                                    },
                        "parentItem": {
                                        "id": %d
                                        },
                        "item_type": "%s"
                        }
                """, item.getSerial(),item.getBrandName().getId(),
                item.getParentItem().getId(),item.getItem_type());

        doAnswer(InvocationOnMock::getArguments).when(itemService).updateItem(anyLong(),Mockito.any(Item.class));
        String url = String.format("/item/update?itemId=%d",id);
        String result = mockMvc.perform(post(url)
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(result).contains(item.getId().toString());
        Mockito.verify(itemService,times(1)).updateItem(anyLong(),Mockito.any(Item.class));
    }

    @Test
    @DisplayName("failed to update item due to brand is not valid")
    void updateItemBrandIsNotValidTest() throws Exception {
        BrandName brand = new BrandName();
        Item parent = new Item(10L,brand,null,ItemType.PACK);
        parent.setId(12L);
        Item item = new Item(100L, brand,parent,ItemType.PACK);
        Long id = 1L;
        doAnswer(InvocationOnMock::getArguments).when(itemService).updateItem(any(Long.class),Mockito.any(Item.class));
        String url = String.format("/item/update?itemId=%d",id);
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
        Mockito.verify(itemService,never()).updateItem(id,item);
    }

    @Test
    @DisplayName("failed to update item due to item_id is not valid")
    void updateItemIdIsNotValidTest() throws Exception {
        BrandName brand = new BrandName("brand", "0.1");
        Item parent = new Item(10L,brand,null,ItemType.PACK);
        parent.setId(12L);
        Item item = new Item(100L, brand,parent,ItemType.ITEM);
        Long id = -1L;
        doAnswer(InvocationOnMock::getArguments).when(itemService).updateItem(any(Long.class),Mockito.any(Item.class));
        String url = String.format("/item/update?itemId=%d",id);
        mockMvc.perform(post(url)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
        Mockito.verify(itemService,never()).updateItem(id,item);
    }

    @Test
    @DisplayName("delete item")
    void deleteItem() throws Exception {
        Long item_id = 1L;
        when(itemService.itemDeleted(item_id)).thenReturn(anyString());
        String url = String.format("/item/delete/%d", item_id);
        mockMvc.perform(get(url)
                        .contentType("text/plain;charset=UTF-8"))
                        .andDo(print())
                        .andExpect(status().isOk()).andReturn();
    }
}