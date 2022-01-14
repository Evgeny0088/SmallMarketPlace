package com.marketPlace.itemstorageservice.exceptions;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ErrorDescription {
    String message;
    List<String> description;
    String status;
    String errorTime;
}
