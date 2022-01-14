package com.marketPlace.itemstorageservice.Services;

import com.marketPlace.itemstorageservice.Models.BrandName;
import com.marketPlace.itemstorageservice.Models.Item;
import com.marketPlace.itemstorageservice.Models.ItemType;
import com.marketPlace.itemstorageservice.Repositories.BrandNameRepo;
import com.marketPlace.itemstorageservice.Repositories.ItemRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceDeleteItemTest {


    @Mock
    private ItemRepo itemRepo;

    @Mock
    private BrandNameRepo brandNameRepo;

    @InjectMocks
    ItemService itemService;

    @Test
    @DisplayName("delete item which does not have parent")
    void itemDeletedNoParentTest(){
        BrandName brand = new BrandName("brand", "0.1");
        brand.setId(1L);
        Item item = new Item(10L,brand,null, ItemType.ITEM);
        item.setId(1L);

        // inputs for delete
        Long id = 1L;
        // check if item id is corresponds with itemDB and brand is exists
        given(itemRepo.findById(id)).willReturn(Optional.of(item));
        given(itemRepo.getById(id)).willReturn(item);

        itemService.itemDeleted(item.getId());

        verify(itemRepo,times(1)).findById(anyLong());
        verify(itemRepo,times(1)).getById(anyLong());
        verify(itemRepo, times(1)).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete item which has parent and parent does not have parent")
    void itemDeletedHasParentTest(){
        BrandName brand = new BrandName("brand", "0.1");
        brand.setId(1L);
        Item parent = new Item(10L,brand,null,ItemType.PACK);
        parent.setId(100L);
        Item item = new Item(10L,brand,parent, ItemType.ITEM);
        item.setId(1L);

        // inputs for delete
        Long id = 1L;

        // check if item id is corresponds with itemDB and brand is exists
        given(itemRepo.findById(id)).willReturn(Optional.of(item));
        given(itemRepo.getById(id)).willReturn(item);

        itemService.itemDeleted(item.getId());

        verify(itemRepo,times(1)).findById(anyLong());
        verify(itemRepo,times(1)).getById(anyLong());
        verify(itemRepo, times(2)).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete item which has parent and parent has parent")
    void itemDeletedParentHasParentTest(){
        BrandName brand = new BrandName("brand", "0.1");
        brand.setId(1L);
        Item parent = new Item(10L,brand,null,ItemType.PACK);
        parent.setId(100L);
        Item parent1 = new Item(10L,brand,parent,ItemType.PACK);
        parent1.setId(101L);
        Item parent2 = new Item(10L,brand,parent1,ItemType.PACK);
        parent2.setId(101L);
        Item item = new Item(10L,brand,parent2, ItemType.ITEM);
        item.setId(1L);

        // inputs for delete
        Long id = 1L;

        // check if item id is corresponds with itemDB and brand is exists
        given(itemRepo.findById(id)).willReturn(Optional.of(item));
        given(itemRepo.getById(id)).willReturn(item);

        itemService.itemDeleted(item.getId());

        verify(itemRepo,times(1)).findById(anyLong());
        verify(itemRepo,times(1)).getById(anyLong());
        verify(itemRepo, times(4)).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete item which has parent and parent has parent")
    void itemDeletedParentDeletedWithAllChildrenTest(){
        BrandName brand = new BrandName("brand", "0.1");
        brand.setId(1L);
        Item parent = new Item(10L,brand,null,ItemType.PACK);
        parent.setId(100L);
        Item item1 = new Item(10L,brand,parent, ItemType.ITEM);
        item1.setId(1L);
        Item item2 = new Item(10L,brand,parent, ItemType.ITEM);
        item2.setId(2L);
        Item item3 = new Item(10L,brand,parent, ItemType.ITEM);
        item3.setId(2L);

        // inputs for delete
        Long id = 100L;

        // check if item id is corresponds with itemDB and brand is exists
        given(itemRepo.findById(id)).willReturn(Optional.of(parent));
        given(itemRepo.getById(id)).willReturn(parent);

        itemService.itemDeleted(parent.getId());

        verify(itemRepo,times(1)).findById(anyLong());
        verify(itemRepo,times(1)).getById(anyLong());
        verify(itemRepo, times(1)).deleteById(anyLong());
    }
}
