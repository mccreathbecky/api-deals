package com.demo.api_deals.model;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealsError extends RuntimeException {
    private String message;
    private String errorCode;
    private Throwable throwable;
    private HttpStatus httpStatus;
}
