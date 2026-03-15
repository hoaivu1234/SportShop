package com.sport.ecommerce.exception.custom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BusinessException extends RuntimeException {
    private final int statusCode;
    private final String message;
}
