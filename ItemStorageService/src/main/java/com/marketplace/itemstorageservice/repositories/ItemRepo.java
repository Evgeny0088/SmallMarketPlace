package com.marketplace.itemstorageservice.repositories;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import com.marketplace.itemstorageservice.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemRepo extends JpaRepository<Item, Long> {

    @Query(nativeQuery = true, name = "findAllItemsDTO")
    List<ItemDetailedInfoDTO> findAllItemsDTO();

    @Query(nativeQuery = true, name = "getItemDTOByParentId")
    ItemDetailedInfoDTO getItemDTOByParentId(@Param("parent_id") Long item_id);

    @Modifying
    @Query(nativeQuery = true,name = "removeItemsFromPackage")
    int removeItemsFromPackage(@Param("parent_id") Long parent_id, @Param("items_count") int items_count);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, name = "removeAllItems")
    void deleteAllItems();
}

