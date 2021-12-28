package com.example.itemstorageservice.utils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class Utils {

    public static HttpHeaders httpHeader(String httpHeader, String headerContent){
        HttpHeaders headers = new HttpHeaders();
        headers.add(httpHeader,headerContent);
        return headers;
    }
}
