package com.junyoung.llm_order_api.maps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "maps")
public record MapsProperties(
                MapsProvider provider,
                String apiKey) {
}
