package com.marketPlace.Orders.Service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@FunctionalInterface
public interface DateProvider {
    LocalDateTime getData();
}
