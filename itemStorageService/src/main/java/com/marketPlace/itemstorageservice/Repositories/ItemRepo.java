package com.marketPlace.itemstorageservice.Repositories;

import com.marketPlace.itemstorageservice.DTOModels.ItemDetailedInfoDTO;
import com.marketPlace.itemstorageservice.Models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepo extends JpaRepository<Item, Long> {

    @Query(nativeQuery = true)
    List<ItemDetailedInfoDTO> findAllItemsDTO();

    // native query
//    select distinct t1.parent_id, t1.serial, t2.children, b.brandname, b.brandversion
//    from items as t1 join (select parent_id, item_type, count(id) as children from items
//    where parent_id is not null group by parent_id,item_type) as t2 on t1.parent_id=t2.parent_id
//    join brands as b on t1.brand_id=b.id where t2.item_type='ITEM' order by t1.parent_id;

}

