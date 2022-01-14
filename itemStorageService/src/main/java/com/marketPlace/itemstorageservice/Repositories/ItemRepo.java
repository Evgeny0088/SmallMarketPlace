package com.marketPlace.itemstorageservice.Repositories;

import com.marketPlace.itemstorageservice.Models.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepo extends JpaRepository<Item, Long> {
}
