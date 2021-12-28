package com.example.itemstorageservice.exceptions;

public class CustomItemsException extends RuntimeException{
        public<T> CustomItemsException(T item, String comment ) {
            super(String.format("object: %s %s", item, comment));
        }
}
