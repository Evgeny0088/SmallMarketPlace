package com.marketplace.itemstorageservice.repositories;

import com.marketplace.itemstorageservice.models.BrandName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface BrandNameRepo extends JpaRepository<BrandName, Long> {

    BrandName findByName(String bname);
}
