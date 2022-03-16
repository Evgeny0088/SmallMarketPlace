package com.marketplace.orders.service;

import com.marketplace.orders.models.PageStatistic;
import com.marketplace.orders.repositories.PageStatisticsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PageStatisticServiceImpl implements PageStatisticService {

    private final PageStatisticsRepo pageStatisticsRepo;

    @Autowired
    public PageStatisticServiceImpl(PageStatisticsRepo pageStatisticsRepo) {
        this.pageStatisticsRepo = pageStatisticsRepo;
    }

    @Override
    public void countOpenMainPages() {
        DateProvider openDate = LocalDateTime::now;
        pageStatisticsRepo.save(new PageStatistic(openDate.getData()));
    }
}
