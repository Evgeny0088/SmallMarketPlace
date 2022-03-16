package com.marketplace.itemstorageservice.services;

import com.marketplace.itemstorageservice.DTOmodels.ItemDetailedInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;

@Component("allPackagesLoader")
@Slf4j
public class LoadAllPackages {

    private final KafkaTemplate<String, List<ItemDetailedInfoDTO>> allItemsListProducer;
    private final ItemDetailedDTOServiceImpl itemDTOservice;
    private final NewTopic allPackagesTopic;

    @Autowired
    public LoadAllPackages(KafkaTemplate<String, List<ItemDetailedInfoDTO>> allItemsListProducer,
                           ItemDetailedDTOServiceImpl itemDTOservice, @Qualifier("allPackagesTopic") NewTopic allPackagesTopic) {
        this.allItemsListProducer = allItemsListProducer;
        this.itemDTOservice = itemDTOservice;
        this.allPackagesTopic = allPackagesTopic;
    }

    public void loadAllItemsFromDB(){
        List<ItemDetailedInfoDTO> allItems = itemDTOservice.allItems();
        ListenableFuture<SendResult<String, List<ItemDetailedInfoDTO>>> future = allItemsListProducer.send(allPackagesTopic.name(),"0",allItems);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, List<ItemDetailedInfoDTO>> result) {
                log.info("{} packages are loaded from DB...", allItems.size());
            }
            @Override
            public void onFailure(Throwable ex) {
                log.error("Packages from database not possible to send!:\n{}",ex.getMessage());
            }
        });
    }
}