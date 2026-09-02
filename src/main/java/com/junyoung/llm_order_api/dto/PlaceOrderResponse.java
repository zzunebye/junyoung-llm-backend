package com.junyoung.llm_order_api.dto;

public record PlaceOrderResponse(
        Long id,
        Integer distance,
        String status) {
}
