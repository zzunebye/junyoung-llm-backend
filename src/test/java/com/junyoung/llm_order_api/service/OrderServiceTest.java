package com.junyoung.llm_order_api.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.junyoung.llm_order_api.dto.PlaceOrderRequest;
import com.junyoung.llm_order_api.dto.TakeOrderRequest;
import com.junyoung.llm_order_api.entity.Order;
import com.junyoung.llm_order_api.entity.OrderStatus;
import com.junyoung.llm_order_api.exceptions.BusinessException;
import com.junyoung.llm_order_api.exceptions.ErrorCode;
import com.junyoung.llm_order_api.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
        @InjectMocks
        private OrderService orderService;

        @Mock
        private OrderRepository orderRepository;

        @Mock
        private DistanceService distanceService;

        @Test
        void placeOrder_returnsOkWithBody() {
                // Given
                var request = new PlaceOrderRequest(
                                List.of("22.3193", "114.1694"),
                                List.of("22.3964", "114.1095"));
                var order = Order.create(
                                22.3193,
                                114.1694,
                                22.3964,
                                114.1095,
                                1000);
                order.setStatus(OrderStatus.UNASSIGNED);
                order.setId(1L);

                when(distanceService.getDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                                .thenReturn(1000);
                when(orderRepository.save(any(Order.class)))
                                .thenReturn(order);

                // When
                var response = orderService.placeOrder(request);

                // Then
                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.distance()).isEqualTo(1000);
                assertThat(response.status()).isEqualTo("UNASSIGNED");
        }

        @Test
        void placeOrder_whenCoordinateIsNotNumeric_shouldThrowValidationError() {
                // Given: in reality, it should be a validation error thrown on controller
                // level.
                var request = new PlaceOrderRequest(
                                List.of("22.3193", "WRONG_NUMBER"),
                                List.of("22.3964", "114.1095"));

                // When-Then
                assertThatThrownBy(() -> orderService.placeOrder(request))
                                .isInstanceOf(BusinessException.class)
                                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                                .isEqualTo(ErrorCode.VALIDATION_ERROR);
                verify(distanceService, never()).getDistance(anyDouble(), anyDouble(), anyDouble(), anyDouble());
                verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void takeOrder_whenOrderIsUnassigned_shouldReturnSuccessStatus() {
                // Given
                var orderId = 1L;
                var request = new TakeOrderRequest("TAKEN");
                var order = Order.create(22.3193, 114.1694, 22.3964, 114.1095, 1000);
                order.setStatus(OrderStatus.UNASSIGNED);

                when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

                // When
                var response = orderService.takeOrder(request, orderId);

                // Then
                assertThat(response.status()).isEqualTo("SUCCESS");
                assertThat(order.getStatus()).isEqualTo(OrderStatus.TAKEN);
        }

        @Test
        void takeOrder_whenOrderIsNotExists_shouldReturnErrorStatus() {
                // Given
                var orderId = 1L;
                var request = new TakeOrderRequest("TAKEN");
                when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

                // Then
                assertThatThrownBy(() -> orderService.takeOrder(request, orderId))
                                .isInstanceOf(BusinessException.class)
                                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);

        }

        @Test
        void takeOrder_whenOrderIsTaken_shouldReturnErrorStatus() {
                // Given
                var orderId = 1L;
                var request = new TakeOrderRequest("TAKEN");
                var order = Order.create(22.3193, 114.1694, 22.3964, 114.1095, 1000);
                order.setStatus(OrderStatus.TAKEN);
                when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> orderService.takeOrder(request, orderId))
                                .isInstanceOf(BusinessException.class)
                                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                                .isEqualTo(ErrorCode.ORDER_ALREADY_TAKEN);

        }

        @Test
        void getOrders_whenPageIsOne_shouldQueryFirstPage() {
                // Given
                when(orderRepository.findAll(any(Pageable.class)))
                                .thenReturn(Page.empty());

                // When
                orderService.getOrders(1, 10);

                // Then
                verify(orderRepository).findAll(PageRequest.of(0, 10));
        }

}
