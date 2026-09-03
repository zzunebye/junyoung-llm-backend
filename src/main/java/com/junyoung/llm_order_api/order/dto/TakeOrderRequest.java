package com.junyoung.llm_order_api.order.dto;

import jakarta.validation.constraints.NotNull;

public record TakeOrderRequest(
        @NotNull String status) {

}
