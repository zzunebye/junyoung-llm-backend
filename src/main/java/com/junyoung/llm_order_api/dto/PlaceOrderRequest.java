package com.junyoung.llm_order_api.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

// The requirement stated that "coordinates in request must be an array of exactly two strings. 
// The type shall only be strings, not integers or floats." However, Spring Boot depends on Jackson library which automatically coerce the conversion of Float and Int to String when the Request param type is String. 

public record PlaceOrderRequest(
        @NotNull @Size(min = 2, max = 2) @JsonDeserialize(contentUsing = StrictStringDeserializer.class)

        List<@NotBlank String> origin,
        @NotNull @Size(min = 2, max = 2) @JsonDeserialize(contentUsing = StrictStringDeserializer.class) List<@NotBlank String> destination) {
}
