package com.example.itemstorageservice.Repositories;

import com.example.itemstorageservice.Models.BrandName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface BrandNameRepo extends JpaRepository<BrandName, Long> {

    BrandName findByName(String bname);

}
