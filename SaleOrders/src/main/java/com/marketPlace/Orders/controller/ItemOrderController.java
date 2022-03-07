package com.marketPlace.Orders.controller;

import com.marketPlace.Orders.DTOModels.ItemDetailedInfoDTO;
import com.marketPlace.Orders.service.ItemProducerService;
import com.marketPlace.Orders.service.PageStatisticService;
import com.marketPlace.Orders.service.PageStatisticServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

import static com.marketPlace.Orders.utils.Utils.httpHeader;

@RestController
@Validated
public class ItemOrderController {

    private final PageStatisticService pageStatisticService;
    private final ItemProducerService itemSoldProduserService;

    @Autowired
    public ItemOrderController(PageStatisticServiceImpl pageStatisticService, ItemProducerService itemSoldProducerService) {
        this.pageStatisticService = pageStatisticService;
        this.itemSoldProduserService = itemSoldProducerService;
    }

    @GetMapping("/brandsMainPage")
    public ResponseEntity<List<ItemDetailedInfoDTO>> allBrands(){
        HttpHeaders header = httpHeader("all brands","list of all brands");
        ResponseEntity<List<ItemDetailedInfoDTO>> brands = ResponseEntity.status(HttpStatus.OK).headers(header).body(null);
        if (brands.getStatusCode().is2xxSuccessful()){
            pageStatisticService.countOpenMainPages();
        }
        return brands;
    }

    @GetMapping("/itemSold/{id}")
    public ResponseEntity<String> itemSold(@Valid @PathVariable("id") Long id, @Valid @RequestParam(value = "quantity") int quantity){
        HttpHeaders header = httpHeader("item is sold",String.format("item with id:%d",id));
        if (itemSoldProduserService.sendSoldItem(id,quantity)){
            return ResponseEntity.status(HttpStatus.OK).headers(header).body("items are sold!...");
        }else {
            return ResponseEntity.status(HttpStatus.OK).headers(header).body("items are not available now!...");
        }
    }
}
