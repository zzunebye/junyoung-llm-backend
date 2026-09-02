package com.junyoung.llm_order_api.exceptions;

public record ErrorResponse(String error) {
    // factory method for easily creating an instance.
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name());
    }
}
