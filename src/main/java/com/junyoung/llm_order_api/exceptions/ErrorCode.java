package com.junyoung.llm_order_api.exceptions;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Can't find order"),
    ORDER_ALREADY_TAKEN(HttpStatus.BAD_REQUEST, "The order is alrady taken"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation error");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

}
