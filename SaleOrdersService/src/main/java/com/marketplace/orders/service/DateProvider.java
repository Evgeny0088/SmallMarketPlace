package com.marketplace.orders.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@FunctionalInterface
public interface DateProvider {
    LocalDateTime getData();
}
