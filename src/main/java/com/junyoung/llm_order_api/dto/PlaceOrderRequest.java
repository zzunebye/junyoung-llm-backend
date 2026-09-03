package com.junyoung.llm_order_api.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * PlaceOrderRequest contains the order's origin and destination coordinates.
 *
 * @param origin      'origin' must be not null. and must have exactly two
 *                    elements. When deserializing, the conversion of Float and
 *                    Int to String is blocked; only String values are accepted.
 *                    Each string in the list must be non-blank and must match
 *                    the number pattern (e.g., "-23.5", "120").
 * @param destination 'destination' must be not null. and must have exactly
 *                    two elements. Only String values are accepted during
 *                    deserialization. Each string must be non-blank and match
 *                    the number pattern (e.g., "-23.5", "120").
 */
public record PlaceOrderRequest(
        @NotNull @Size(min = 2, max = 2) @JsonDeserialize(contentUsing = StrictStringDeserializer.class) List<@NotNull @Pattern(regexp = "^-?\\d+(\\.\\d+)?$") String> origin,

        @NotNull @Size(min = 2, max = 2) @JsonDeserialize(contentUsing = StrictStringDeserializer.class) List<@NotNull @Pattern(regexp = "^-?\\d+(\\.\\d+)?$") String> destination) {
}
