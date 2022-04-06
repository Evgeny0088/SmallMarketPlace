package com.marketplace.itemstorageservice.backupTests;

import com.marketplace.itemstorageservice.exceptions.CustomItemsException;
import com.marketplace.itemstorageservice.models.BrandName;
import com.marketplace.itemstorageservice.models.Item;
import com.marketplace.itemstorageservice.models.ItemType;
import com.marketplace.itemstorageservice.repositories.BrandNameRepo;
import com.marketplace.itemstorageservice.repositories.ItemRepo;
import com.marketplace.itemstorageservice.services.ItemServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
class ItemServiceCreateItem_BACKUPTest {

    @Mock
    private ItemRepo itemRepo;

    @Mock
    private BrandNameRepo brandNameRepo;

    @InjectMocks
    ItemServiceImpl itemService;

    @Test
    @DisplayName("get all items")
    void allItemsTest() {
        List<Item> items = new ArrayList<>();
        BrandName brand = new BrandName("brand", "0.1");
        Item parent = new Item(10L, brand, null, ItemType.PACK);
        parent.setId(100L);
        items.add(parent);
        items.add(new Item(1L, brand, parent, ItemType.ITEM));
        items.add(new Item(2L, brand, parent, ItemType.ITEM));
        items.add(new Item(3L, brand, parent, ItemType.ITEM));
        given(itemRepo.findAll()).willReturn(items);
        List<Item> excepted = itemService.allItems();
        assertEquals(items, excepted);
    }

    @Test
    @DisplayName("create new item when parent is null")
    void createNewItemTest() {
        BrandName brand = new BrandName("brand", "0.1");
        brand.setId(1L);
        Item item = new Item(1L, brand, null, ItemType.PACK);
        assertNotNull(item.getBrandName());
        assertNull(item.getParentItem());

        given(brandNameRepo.findById(item.getBrandName().getId())).willReturn(Optional.of(brand));
        given(brandNameRepo.getById(item.getBrandName().getId())).willReturn(brand);
        given(itemRepo.save(item)).willAnswer(invocation -> invocation.getArgument(0));
        itemService.createNewItem(item);

        verify(brandNameRepo, times(1)).findById(anyLong());
        verify(brandNameRepo, times(1)).getById(anyLong());
        verify(itemRepo, times(1)).save(Mockito.any(Item.class));
    }

    @Test
    @DisplayName("create new item when parent exists and parent type is PACK")
    void createNewItemParentExistsTest() {
        BrandName brand = new BrandName("brand", "0.1");
        brand.setId(1L);
        Item parent = new Item(10L, brand, null, ItemType.PACK);
        parent.setId(100L);
        Item item = new Item(1L, brand, parent, ItemType.ITEM);

        // check if brand is valid and exists
        assertNotNull(item.getBrandName());
        given(brandNameRepo.findById(item.getBrandName().getId())).willReturn(Optional.of(brand));
        given(brandNameRepo.getById(item.getBrandName().getId())).willReturn(brand);

        // check if parent is valid and exists and parent type must be PACK
        given(itemRepo.findById(item.getParentItem().getId())).willReturn(Optional.of(parent));
        assertSame(item.getParentItem().getItem_type(), ItemType.PACK);
        given(itemRepo.save(item.getParentItem())).willAnswer(invocation -> invocation.getArgument(0));
        itemService.createNewItem(item);

        assertTrue(item.getParentItem().getChildItems().contains(item));
        verify(brandNameRepo, times(1)).findById(anyLong());
        verify(brandNameRepo, times(1)).getById(anyLong());
        verify(itemRepo, times(1)).findById(anyLong());
        verify(itemRepo, times(1)).save(Mockito.any(Item.class));
    }

    @Test
    @DisplayName("failed to create item due to brand is not the same as brand in parent item")
    void createNewFailedParentBrandIsNotAsItemBrand() {
        BrandName brandInParent = new BrandName("brand", "0.1");
        brandInParent.setId(1L);
        BrandName brandInItem = new BrandName("newBrand", "0.1");
        brandInParent.setId(2L);
        Item parent = new Item(10L, brandInParent, null, ItemType.PACK);
        parent.setId(100L);
        Item item = new Item(1L, brandInItem, parent, ItemType.ITEM);

        InOrder order = Mockito.inOrder(brandNameRepo, itemRepo);

        // check if brand is valid and exists
        assertNotNull(item.getBrandName());
        given(brandNameRepo.findById(item.getBrandName().getId())).willReturn(Optional.of(brandInItem));
        given(brandNameRepo.getById(item.getBrandName().getId())).willReturn(brandInItem);
        given(itemRepo.findById(item.getParentItem().getId())).willReturn(Optional.of(parent));

        assertNotSame(item.getBrandName(), parent.getBrandName());
        assertThrows(CustomItemsException.class, () -> itemService.createNewItem(item));

        order.verify(brandNameRepo, times(0)).findById(anyLong());
        order.verify(brandNameRepo, times(0)).getById(anyLong());
        order.verify(itemRepo, times(1)).findById(anyLong());
        order.verify(itemRepo, never()).save(Mockito.any(Item.class));
    }

    @Test
    @DisplayName("failed to create new item when parent exists but type is ITEM")
    void createNewItemFailedParentExistsButTypeIsITEMTest() {
        BrandName brand = new BrandName("brand", "0.1");
        brand.setId(1L);
        Item parent = new Item(10L, brand, null, ItemType.ITEM);
        parent.setId(100L);
        Item item = new Item(1L, brand, parent, ItemType.ITEM);

        // check if brand is valid and exists
        assertNotNull(item.getBrandName());
        given(brandNameRepo.findById(item.getBrandName().getId())).willReturn(Optional.of(brand));
        given(brandNameRepo.getById(item.getBrandName().getId())).willReturn(brand);

        // check if parent is valid and exists and parent type is ITEM
        given(itemRepo.findById(item.getParentItem().getId())).willReturn(Optional.of(parent));

        assertThrows(CustomItemsException.class, () -> itemService.createNewItem(item));

        verify(brandNameRepo, times(1)).findById(anyLong());
        verify(brandNameRepo, times(1)).getById(anyLong());
        verify(itemRepo, times(1)).findById(anyLong());
        verify(itemRepo, never()).save(Mockito.any(Item.class));
    }

    @Test
    @DisplayName("failed to create new item when brand not found in DB")
    void createNewItemFaledParentExistsButTypeIsITEMTest() {
        BrandName brand = new BrandName("brand", "0.1");
        brand.setId(1L);
        Item parent = new Item(10L, brand, null, ItemType.PACK);
        parent.setId(100L);
        Item item = new Item(1L, brand, parent, ItemType.ITEM);
        // check if brand is not null but not found in DB
        assertNotNull(item.getBrandName());
        given(brandNameRepo.findById(item.getBrandName().getId())).willReturn(Optional.empty());
        assertThrows(CustomItemsException.class, () -> itemService.createNewItem(item));

        verify(brandNameRepo, times(1)).findById(anyLong());
        verify(itemRepo, never()).save(Mockito.any(Item.class));
    }
}