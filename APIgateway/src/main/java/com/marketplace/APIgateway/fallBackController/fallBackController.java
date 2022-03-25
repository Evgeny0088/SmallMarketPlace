package com.marketplace.APIgateway.fallBackController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class fallBackController {

    @GetMapping("/fallback-for-itemStorageService")
    public ResponseEntity<String> fallBackCallFromItemStorageService(){
        String message = "Item-Storage service is not available now, please check later!...";
        HttpHeaders headers = httpHeaders("item storage service","");
        log.warn(message);
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).headers(headers)
                .body(message);
    }

    @GetMapping("/fallback-for-saleOrdersService")
    public ResponseEntity<String> fallBackCallFromSaleOrdersService(){
        String message = "Sale-Orders service is not available now, please check later!...";
        log.warn(message);
        HttpHeaders headers = httpHeaders("sale orders service","");
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).headers(headers)
                .body(message);
    }

    private static HttpHeaders httpHeaders(String head, String headerContent){
        HttpHeaders header = new HttpHeaders();
        header.add(head, headerContent);
        return header;
    }
}
