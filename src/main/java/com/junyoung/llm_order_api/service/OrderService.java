package com.junyoung.llm_order_api.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final DistanceService distanceService;
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderService(OrderRepository orderRepository, DistanceService distanceService) {
        this.orderRepository = orderRepository;
        this.distanceService = distanceService;
    }

    @Transactional
    public PlaceOrderResponse placeOrder(@Valid PlaceOrderRequest request) {
        double startLat = Double.parseDouble(request.origin().get(0));
        double startLon = Double.parseDouble(request.origin().get(1));
        double endLat = Double.parseDouble(request.destination().get(0));
        double endLon = Double.parseDouble(request.destination().get(1));
        int distance = distanceService.getDistance(startLat, startLon, endLat, endLon);
        Order order = Order.create(startLat, startLon, endLat, endLon, distance);
        orderRepository.save(order);

        return new PlaceOrderResponse(
                order.getId(),
                order.getDistance(),
                order.getStatus().name());
    }

    @Transactional
    public TakeOrderResponse takeOrder(TakeOrderRequest request, Long orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus().equals(OrderStatus.TAKEN)) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_TAKEN);
        }

        order.setStatus(OrderStatus.TAKEN);
        orderRepository.save(order);

        return new TakeOrderResponse("SUCCESS");
    }

    public List<OrderResponse> getOrders(
            int page,
            int limit) {
        PageRequest pageable = PageRequest.of(page, limit);
        return orderRepository.findAll(pageable).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

}
