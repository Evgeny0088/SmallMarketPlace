package com.marketPlace.itemstorageservice.services;

import com.marketPlace.itemstorageservice.DTOModels.ItemDetailedInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;

@Service
@Slf4j
public class KafkaProducerDTOServiceImpl implements KafkaProducerService{

    @Value("${itemDTO.topic.name_2}")
    private String updateItemDTOTopic;

    private KafkaTemplate<String, ItemDetailedInfoDTO> updateItemKafkaTemplate;

    @Autowired
    public KafkaProducerDTOServiceImpl(KafkaTemplate<String, ItemDetailedInfoDTO> updateItemKafkaTemplate) {
        this.updateItemKafkaTemplate = updateItemKafkaTemplate;
    }

    @Override
    public void sendItemDTO(ItemDetailedInfoDTO itemDTO) {

        ListenableFuture<SendResult<String, ItemDetailedInfoDTO>> future
                = this.updateItemKafkaTemplate.send(updateItemDTOTopic, itemDTO);

        future.addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onSuccess(SendResult<String, ItemDetailedInfoDTO> result) {
                log.info(String.format("ItemDTO is updated with id<%d>, with offset:%d"
                        ,itemDTO.getItemPackageId(), result.getRecordMetadata().offset()));
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error(String.format("itemDTO with id <%d> is failed, see exception:\n%s",
                        itemDTO.getItemPackageId(),ex.getCause()));
            }});
    }

    @Override
    public void sendAllItemsDTOPackages(List<ItemDetailedInfoDTO> itemsDTO) {

    }
}
