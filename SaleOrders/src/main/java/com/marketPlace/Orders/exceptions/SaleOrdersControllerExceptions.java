package com.marketPlace.Orders.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestControllerAdvice
public class SaleOrdersControllerExceptions extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(errorCollector("Method not supported error",ex.getMessage(),status));
    }

    private <T> ErrorDescription errorCollector(String description, T ex, HttpStatus status){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd' Local Time: 'hh:mm");
        String dateFormatted = LocalDateTime.now().format(fmt);
        return new ErrorDescription.ErrorDescriptionBuilder()
                .message(ex.toString())
                .description(List.of(description))
                .status(status.name())
                .errorTime(dateFormatted)
                .build();
    }
}
