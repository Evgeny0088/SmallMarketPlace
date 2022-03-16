package com.marketplace.itemstorageservice.exceptions;

import lombok.*;
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
