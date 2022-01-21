package com.marketPlace.Orders.Service;

import com.marketPlace.Orders.Models.PageStatistic;
import com.marketPlace.Orders.Repositories.PageStatisticsRepo;
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
