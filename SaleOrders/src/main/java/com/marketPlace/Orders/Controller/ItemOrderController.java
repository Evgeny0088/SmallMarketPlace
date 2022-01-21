package com.marketPlace.Orders.Controller;

import com.marketPlace.Orders.DTOModels.ItemDetailedInfoDTO;
import com.marketPlace.Orders.Service.PageStatisticService;
import com.marketPlace.Orders.Service.PageStatisticServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.marketPlace.Orders.utils.Utils.httpHeader;

@RestController
@Validated
public class ItemOrderController {

    private final PageStatisticService pageStatisticService;

    @Autowired
    public ItemOrderController(PageStatisticServiceImpl pageStatisticService) {
        this.pageStatisticService = pageStatisticService;
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

}
