package com.junyoung.llm_order_api.dto;

import com.junyoung.llm_order_api.entity.Order;
import com.junyoung.llm_order_api.entity.OrderStatus;

public record OrderResponse(
                Long id,
                Integer distance,
                OrderStatus status) {

        public static OrderResponse from(Order order) {
                return new OrderResponse(order.getId(), order.getDistance(), order.getStatus());
        }

}
