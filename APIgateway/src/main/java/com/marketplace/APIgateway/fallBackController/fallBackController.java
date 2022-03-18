package com.marketplace.APIgateway.fallBackController;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class fallBackController {

    @GetMapping("/fallback-for-itemStorageService")
    public ResponseEntity<String> fallBackCallFromItemStorageService(){
        HttpHeaders headers = httpHeaders("item storage service","");
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).headers(headers)
                .body("Item-Storage service is not available now, please check later!...");
    }

    @GetMapping("/fallback-for-saleOrdersService")
    public ResponseEntity<String> fallBackCallFromSaleOrdersService(){
        HttpHeaders headers = httpHeaders("sale orders service","");
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).headers(headers)
                .body("Sale-Orders service is not available now, please check later!...");
    }

    private static HttpHeaders httpHeaders(String head, String headerContent){
        HttpHeaders header = new HttpHeaders();
        header.add(head, headerContent);
        return header;
    }
}
