package com.example.itemstorageservice.Repositories;

import com.example.itemstorageservice.Models.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepo extends JpaRepository<Item, Long> {
}
