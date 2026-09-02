package com.junyoung.llm_order_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.junyoung.llm_order_api.dto.OrderResponse;
import com.junyoung.llm_order_api.dto.PlaceOrderRequest;
import com.junyoung.llm_order_api.dto.PlaceOrderResponse;
import com.junyoung.llm_order_api.dto.TakeOrderRequest;
import com.junyoung.llm_order_api.dto.TakeOrderResponse;
import com.junyoung.llm_order_api.service.OrderService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public PlaceOrderResponse placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(request);
    }

    @PatchMapping("/{id}")
    public TakeOrderResponse takeOrder(
            @Valid @RequestBody TakeOrderRequest request,
            @PathVariable("id") @NotNull Long id) {

        return orderService.takeOrder(request, id);
    }

    @GetMapping
    public List<OrderResponse> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return orderService.getOrders(page, limit);
    }

}
