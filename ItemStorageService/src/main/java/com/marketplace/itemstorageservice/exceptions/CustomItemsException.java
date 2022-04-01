package com.marketplace.itemstorageservice.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomItemsException extends RuntimeException{

    private final HttpStatus errorStatus;

    public<T> CustomItemsException(T item, String comment, HttpStatus errorStatus) {
        super(String.format("object: %s, %s", item, comment));
        this.errorStatus = errorStatus;
    }
}
