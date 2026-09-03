package com.junyoung.llm_order_api.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@Validated
public class OrderService {
    private final OrderRepository orderRepository;
    private final DistanceService distanceService;

    public OrderService(OrderRepository orderRepository, DistanceService distanceService) {
        this.orderRepository = orderRepository;
        this.distanceService = distanceService;
    }

    private static boolean outOfRange(double lat, double lon) {
        return lat < -90 || lat > 90 || lon < -180 || lon > 180;
    }

    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        try {
            double startLat = Double.parseDouble(request.origin().get(0));
            double startLon = Double.parseDouble(request.origin().get(1));
            double endLat = Double.parseDouble(request.destination().get(0));
            double endLon = Double.parseDouble(request.destination().get(1));

            if (outOfRange(startLat, startLon) || outOfRange(endLat, endLon)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR);
            }

            int distance = distanceService.getDistance(startLat, startLon, endLat, endLon);
            Order order = Order.create(startLat, startLon, endLat, endLon, distance);
            Order savedOrder = orderRepository.save(order);
            return new PlaceOrderResponse(
                    savedOrder.getId(),
                    savedOrder.getDistance(),
                    savedOrder.getStatus().name());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

    }

    @Transactional
    public TakeOrderResponse takeOrder(TakeOrderRequest request, Long orderId) {
        if (!OrderStatus.TAKEN.name().equals(request.status())) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS_UPDATE);
        }

        int updated = orderRepository
                .takeOrder(orderId, OrderStatus.TAKEN, OrderStatus.UNASSIGNED);

        if (updated == 0) {
            if (!orderRepository.existsById(orderId)) {
                throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
            }
            throw new BusinessException(ErrorCode.ORDER_ALREADY_TAKEN);
        }
        return new TakeOrderResponse("SUCCESS");
    }

    public List<OrderResponse> getOrders(
            int page,
            int limit) {
        PageRequest pageable = PageRequest.of(page - 1, limit);
        return orderRepository.findAll(pageable).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

}
