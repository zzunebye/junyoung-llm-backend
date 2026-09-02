package com.junyoung.llm_order_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.junyoung.llm_order_api.dto.OrderResponse;
import com.junyoung.llm_order_api.dto.PlaceOrderRequest;
import com.junyoung.llm_order_api.dto.PlaceOrderResponse;
import com.junyoung.llm_order_api.dto.TakeOrderRequest;
import com.junyoung.llm_order_api.dto.TakeOrderResponse;
import com.junyoung.llm_order_api.entity.OrderStatus;
import com.junyoung.llm_order_api.exceptions.GlobalExceptionHandler;
import com.junyoung.llm_order_api.service.OrderService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
public class OrderControllerTest {
        @Autowired
        MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;
        @MockitoBean
        private OrderService orderService;

        @Test
        void getOrders_returnsOk() throws Exception {
                when(orderService.getOrders(anyInt(), anyInt()))
                                .thenReturn(List.of(new OrderResponse(1L, 1000, OrderStatus.UNASSIGNED)));

                mockMvc.perform(get("/orders")
                                .param("page", "1")
                                .param("limit", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].distance").value(1000))
                                .andExpect(jsonPath("$[0].status").value("UNASSIGNED"));
        }

        @Test
        void getOrders_returnsOk_WhenNoPageAndLimit() throws Exception {
                // given
                var orders = List.of(new OrderResponse(1L, 1000, OrderStatus.UNASSIGNED));
                when(orderService.getOrders(anyInt(), anyInt()))
                                .thenReturn(orders);

                // when
                var action = mockMvc.perform(get("/orders"));
                // then
                action.andExpect(status().isOk())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].distance").value(1000))
                                .andExpect(jsonPath("$[0].status").value("UNASSIGNED"));
        }

        @Test
        void getOrders_returnsBadRequest_WhenWrongArgOnPage() throws Exception {
                // given
                when(orderService.getOrders(anyInt(), anyInt()))
                                .thenReturn(List.of(new OrderResponse(1L, 1000, OrderStatus.UNASSIGNED)));

                // when
                var action = mockMvc.perform(get("/orders")
                                .param("page", "one"));
                // then
                action.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
                verifyNoInteractions(orderService);
        }

        @Test
        void getOrders_returnsBadRequest_WhenWrongArgOnLimit() throws Exception {
                // given
                var order = new OrderResponse(1L, 1000, OrderStatus.UNASSIGNED);
                when(orderService.getOrders(anyInt(), anyInt()))
                                .thenReturn(List.of(order));

                // when
                var action = mockMvc.perform(get("/orders")
                                .param("limit", "one"));
                // then
                action.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
                verifyNoInteractions(orderService);
        }

        @Test
        void getOrders_returnsBadRequest_WhenPageIsZero() throws Exception {
                // given
                var action = mockMvc.perform(get("/orders")
                                .param("page", "0"));

                action.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

                verifyNoInteractions(orderService);
        }

        @Test
        void getOrders_returnsBadRequest_WhenLimitIsZero() throws Exception {
                var action = mockMvc.perform(get("/orders")
                                .param("limit", "0"));
                action.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
                verifyNoInteractions(orderService);
        }

        @Test
        void placeOrder_returnsOk() throws Exception {
                // given
                var request = new PlaceOrderRequest(List.of("22.3193", "114.1694"), List.of("22.3964", "114.1095"));
                when(orderService.placeOrder(any(PlaceOrderRequest.class)))
                                .thenReturn(new PlaceOrderResponse(1L, 1000, "UNASSIGNED"));

                // when
                ResultActions action = mockMvc.perform(post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)));
                // then
                action
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.distance").value(1000))
                                .andExpect(jsonPath("$.status").value("UNASSIGNED"));
                verify(orderService).placeOrder(request);
        }

        @Test
        void placeOrder_returnsBadRequest_WhenCoordinateAreMissing() throws Exception {
                // given
                var request = new PlaceOrderRequest(List.of("22.3193"), List.of("22.3964", "114.1095"));
                // when
                var action = mockMvc.perform(post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)));
                // then
                action.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void placeOrder_returnsBadRequest_WhenCoordinateIsNotString() throws Exception {

                mockMvc.perform(post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                        {
                                                          "origin": ["22.3193", 114.1694],
                                                          "destination": ["22.3964", "114.1095"]
                                                        }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void placeOrder_returnsBadRequest_WhenCoordinateWithWrongString() throws Exception {
                // given
                var request = new PlaceOrderRequest(List.of("22.3193", "114.1095"), List.of("22.3964", "WRONG_NUMBER"));
                // when
                var action = mockMvc.perform(post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)));
                // then
                action.andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void takeOrder_returnsOk() throws Exception {
                // given
                var request = new TakeOrderRequest("TAKEN");
                when(orderService.takeOrder(any(TakeOrderRequest.class), anyLong()))
                                .thenReturn(new TakeOrderResponse("SUCCESS"));
                // when
                var action = mockMvc.perform(patch("/orders/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)));
                // then
                action.andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUCCESS"));
                verify(orderService).takeOrder(request, 1L);
        }
}
