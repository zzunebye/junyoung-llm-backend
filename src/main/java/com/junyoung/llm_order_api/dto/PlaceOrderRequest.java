package com.junyoung.llm_order_api.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlaceOrderRequest(
        @NotNull @Size(min = 2, max = 2) List<String> origin,
        @NotNull @Size(min = 2, max = 2) List<String> destination

) {
}
