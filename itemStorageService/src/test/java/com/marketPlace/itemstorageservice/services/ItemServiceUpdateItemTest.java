package com.marketPlace.itemstorageservice.services;

import com.marketPlace.itemstorageservice.models.BrandName;
import com.marketPlace.itemstorageservice.models.Item;
import com.marketPlace.itemstorageservice.models.ItemType;
import com.marketPlace.itemstorageservice.repositories.BrandNameRepo;
import com.marketPlace.itemstorageservice.repositories.ItemRepo;
import com.marketPlace.itemstorageservice.exceptions.CustomItemsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceUpdateItemTest {

    @Mock
    private ItemRepo itemRepo;

    @Mock
    private BrandNameRepo brandNameRepo;

    @InjectMocks
    ItemServiceImpl itemService;

    @Test
    @DisplayName("update item with inputs where parent is null, new serial, new brand and item type is ITEM")
    void updateItemTest(){
        BrandName brandOld = new BrandName("brand", "0.1");
        brandOld.setId(1L);
        BrandName brandNew = new BrandName("brand", "0.1");
        brandNew.setId(1L);
        Item parent = new Item(10L,brandOld,null,ItemType.PACK);
        parent.setId(100L);
        Long id = 1L;
        Item itemFromDB = new Item(12L,brandOld,parent,ItemType.PACK);
        itemFromDB.setId(id);

        // inputs for update
        Item item = new Item(1L,brandNew, null, ItemType.ITEM);

        // check if item id is corresponds with itemDB and brand is exists
        given(itemRepo.findById(id)).willReturn(Optional.of(itemFromDB));
        given(brandNameRepo.findById(itemFromDB.getBrandName().getId())).willReturn(Optional.of(brandOld));
        given(brandNameRepo.getById(itemFromDB.getBrandName().getId())).willReturn(brandOld);

        given(itemRepo.save(itemFromDB)).willAnswer(invocation -> invocation.getArgument(0));
        itemService.updateItem(id,item);

        assertSame(itemFromDB.getSerial(),item.getSerial());
        assertSame(itemFromDB.getItem_type(), ItemType.ITEM);
        assertNull(itemFromDB.getParentItem());

        verify(itemRepo,times(1)).findById(anyLong());
        verify(brandNameRepo, times(1)).findById(anyLong());
        verify(brandNameRepo, times(1)).getById(anyLong());
        verify(itemRepo, times(1)).save(Mockito.any(Item.class));

    }

    @Test
    @DisplayName("failed to update item where inputs - parent type is ITEM")
    void updateItemParentItemTypeIsITEMTest(){
        BrandName brand = new BrandName("brand", "0.1");
        brand.setId(1L);
        Item parentOld = new Item(10L,brand,null,ItemType.PACK);
        parentOld.setId(100L);
        Item parentNew = new Item(10L,brand,null,ItemType.ITEM);
        parentNew.setId(101L);
        Long id = 1L;
        Item itemFromDB = new Item(12L,brand,parentOld,ItemType.PACK);
        itemFromDB.setId(id);

        // inputs for update
        Item item = new Item(1L,brand, parentNew, ItemType.ITEM);

        // check if id is corresponds with itemDB and brand is exists
        given(itemRepo.findById(id)).willReturn(Optional.of(itemFromDB));
        given(brandNameRepo.findById(item.getBrandName().getId())).willReturn(Optional.of(brand));
        given(brandNameRepo.getById(item.getBrandName().getId())).willReturn(brand);
        given(itemRepo.findById(item.getParentItem().getId())).willReturn(Optional.of(parentNew));

        assertThrows(CustomItemsException.class,()->itemService.updateItem(id,item));

        assertSame(itemFromDB.getParentItem(), parentOld);

        verify(itemRepo,times(2)).findById(anyLong());
        verify(brandNameRepo, times(1)).findById(anyLong());
        verify(brandNameRepo, times(1)).getById(anyLong());
        verify(itemRepo, never()).save(Mockito.any(Item.class));
    }

    @Test
    @DisplayName("failed to update item where brand not valid")
    void updateItemBrandIsNotValidTest(){
        BrandName brand = new BrandName("brand", "0.1");
        brand.setId(1L);
        Item parent = new Item(10L,brand,null,ItemType.PACK);
        parent.setId(100L);
        Long id = 1L;
        Item itemFromDB = new Item(12L,brand,parent,ItemType.PACK);
        itemFromDB.setId(id);

        // inputs for update
        BrandName brandNotValid = new BrandName("brand not valid", "0.1");
        brandNotValid.setId(2L);
        Item item = new Item(1L,brandNotValid, parent, ItemType.ITEM);

        // check if id is corresponds with itemDB and brand is exists
        given(itemRepo.findById(id)).willReturn(Optional.of(itemFromDB));
        given(brandNameRepo.findById(item.getBrandName().getId())).willReturn(Optional.empty());

        assertThrows(CustomItemsException.class,()->itemService.updateItem(id,item));

        verify(itemRepo,times(2)).findById(anyLong());
        verify(brandNameRepo, times(1)).findById(anyLong());
        verify(itemRepo, never()).save(Mockito.any(Item.class));
    }

    @Test
    @DisplayName("failed to update item due to brand is not the same as brand in parent item")
    void updateItemFailedParentBrandIsNotAsItemBrand() {
        BrandName brandInParent = new BrandName("brand", "0.1");
        brandInParent.setId(1L);
        Item parent = new Item(10L, brandInParent, null, ItemType.PACK);
        parent.setId(100L);
        Item itemFromDB = new Item(12L,brandInParent,parent,ItemType.PACK);
        Long id = 1L;
        itemFromDB.setId(id);

        // inputs for update
        BrandName brandInItem = new BrandName("newBrand", "0.1");
        brandInParent.setId(2L);
        Item item = new Item(1L, brandInItem, parent, ItemType.ITEM);

        InOrder order = Mockito.inOrder(brandNameRepo, itemRepo);

        // check if brand is valid and exists
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

}