package com.junyoung.llm_order_api.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.validation.annotation.Validated;

import org.springframework.stereotype.Service;

import com.junyoung.llm_order_api.dto.OrderResponse;
import com.junyoung.llm_order_api.dto.PlaceOrderRequest;
import com.junyoung.llm_order_api.dto.PlaceOrderResponse;
import com.junyoung.llm_order_api.dto.TakeOrderRequest;
import com.junyoung.llm_order_api.dto.TakeOrderResponse;
import com.junyoung.llm_order_api.entity.Order;
import com.junyoung.llm_order_api.entity.OrderStatus;
import com.junyoung.llm_order_api.exceptions.BusinessException;
import com.junyoung.llm_order_api.exceptions.ErrorCode;
import com.junyoung.llm_order_api.repository.OrderRepository;

import jakarta.validation.Valid;

@Service
@Validated
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public PlaceOrderResponse placeOrder(@Valid PlaceOrderRequest request) {
        Order order = new Order();
        order.setStartLatitude(Double.parseDouble(request.origin().get(0)));
        order.setStartLongitude(Double.parseDouble(request.origin().get(1)));
        order.setEndLatitude(Double.parseDouble(request.destination().get(0)));
        order.setEndLongitude(Double.parseDouble(request.destination().get(1)));
        order.setDistance(0);
        order.setCreatedAt(Instant.now());

        return new PlaceOrderResponse(orderRepository.save(order).getId(), order.getDistance(),
                order.getStatus().name());
    }

    public TakeOrderResponse takeOrder(TakeOrderRequest request, Long orderId) {
        if (request.status().equals("TAKEN")) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_TAKEN);
        }

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        order.setStatus(OrderStatus.TAKEN);
        orderRepository.save(order);
        return new TakeOrderResponse("SUCCESS");
    }

    public List<OrderResponse> getOrders(
            String page,
            String limit) {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

}
