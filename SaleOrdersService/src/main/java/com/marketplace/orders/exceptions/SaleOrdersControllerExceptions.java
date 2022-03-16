package com.marketplace.orders.exceptions;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice
public class SaleOrdersControllerExceptions extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(errorCollector("Method not supported error",ex.getMessage(),status));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(errorCollector("request body is wrong",ex.getMessage(),status));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(errorCollector("Method type not supported error",ex.getMessage(),status));
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(errorCollector("Path variable is missing", Objects.requireNonNull(ex.getMessage()),status));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(errorCollector("Request parameter is missing", Objects.requireNonNull(ex.getMessage()),status));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(errorCollector("Type mismatch error", Objects.requireNonNull(ex.getMessage()), status));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(errorCollector("Http message not readable", Objects.requireNonNull(ex.getMessage()), status));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(errorCollector("Http message not writable", Objects.requireNonNull(ex.getMessage()), status));
    }


    @ExceptionHandler(CustomItemsException.class)
    @ResponseBody
    public ResponseEntity<Object> createItemException(CustomItemsException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorCollector("Package not valid!", Objects.requireNonNull(ex.getMessage()),HttpStatus.BAD_REQUEST));
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
