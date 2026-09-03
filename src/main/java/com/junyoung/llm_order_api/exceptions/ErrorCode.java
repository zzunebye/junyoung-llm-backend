package com.junyoung.llm_order_api.exceptions;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND),
    ORDER_ALREADY_TAKEN(HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS_UPDATE(HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND),
    DISTANCE_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
