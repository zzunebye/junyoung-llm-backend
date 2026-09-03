package com.junyoung.llm_order_api.order.dto;

public record PlaceOrderResponse(
                Long id,
                Integer distance,
                String status) {
}
