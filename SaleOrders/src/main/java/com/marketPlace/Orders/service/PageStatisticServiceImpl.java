package com.marketPlace.Orders.service;

import com.marketPlace.Orders.models.PageStatistic;
import com.marketPlace.Orders.repositories.PageStatisticsRepo;
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
