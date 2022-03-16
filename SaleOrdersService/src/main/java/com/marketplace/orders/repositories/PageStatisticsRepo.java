package com.marketplace.orders.repositories;

import com.marketplace.orders.models.PageStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageStatisticsRepo extends JpaRepository<PageStatistic, Long> {

}
