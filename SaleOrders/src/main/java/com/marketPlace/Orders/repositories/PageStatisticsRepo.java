package com.marketPlace.Orders.repositories;

import com.marketPlace.Orders.models.PageStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageStatisticsRepo extends JpaRepository<PageStatistic, Long> {

}
