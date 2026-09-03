package com.junyoung.llm_order_api.order;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.junyoung.llm_order_api.distance.DistanceService;
import com.junyoung.llm_order_api.exceptions.BusinessException;
import com.junyoung.llm_order_api.exceptions.ErrorCode;
import com.junyoung.llm_order_api.order.dto.PlaceOrderRequest;
import com.junyoung.llm_order_api.order.dto.TakeOrderRequest;
import com.junyoung.llm_order_api.order.entity.Order;
import com.junyoung.llm_order_api.order.entity.OrderStatus;

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

                when(orderRepository.takeOrder(orderId, OrderStatus.TAKEN, OrderStatus.UNASSIGNED)).thenReturn(1);

                // When
                var response = orderService.takeOrder(request, orderId);

                // Then
                verify(orderRepository).takeOrder(orderId, OrderStatus.TAKEN, OrderStatus.UNASSIGNED);
                verify(orderRepository, never()).existsById(orderId);
                assertThat(response.status()).isEqualTo("SUCCESS");
        }

        @Test
        void takeOrder_whenOrderIsNotExists_shouldReturnErrorStatus() {
                // Given
                var orderId = 1L;
                var request = new TakeOrderRequest("TAKEN");
                when(orderRepository.takeOrder(orderId, OrderStatus.TAKEN, OrderStatus.UNASSIGNED)).thenReturn(0);
                when(orderRepository.existsById(orderId)).thenReturn(false);

                // When-Then
                assertThatThrownBy(() -> orderService.takeOrder(request, orderId))
                                .isInstanceOf(BusinessException.class)
                                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
                verify(orderRepository).takeOrder(orderId, OrderStatus.TAKEN, OrderStatus.UNASSIGNED);
                verify(orderRepository).existsById(orderId);
        }

        @Test
        void takeOrder_whenOrderIsTaken_shouldReturnErrorStatus() {
                // Given
                var orderId = 1L;
                var request = new TakeOrderRequest("TAKEN");
                var order = Order.create(22.3193, 114.1694, 22.3964, 114.1095, 1000);
                order.setStatus(OrderStatus.TAKEN);
                order.setId(orderId);
                when(orderRepository.takeOrder(orderId, OrderStatus.TAKEN, OrderStatus.UNASSIGNED)).thenReturn(0);
                when(orderRepository.existsById(orderId)).thenReturn(true);

                // When-Then
                assertThatThrownBy(() -> orderService.takeOrder(request, orderId))
                                .isInstanceOf(BusinessException.class)
                                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                                .isEqualTo(ErrorCode.ORDER_ALREADY_TAKEN);
                verify(orderRepository).takeOrder(orderId, OrderStatus.TAKEN, OrderStatus.UNASSIGNED);
                verify(orderRepository).existsById(orderId);
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
