package com.junyoung.llm_order_api.integration;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;

import com.junyoung.llm_order_api.dto.PlaceOrderRequest;
import com.junyoung.llm_order_api.dto.TakeOrderRequest;
import com.junyoung.llm_order_api.entity.Order;
import com.junyoung.llm_order_api.entity.OrderStatus;
import com.junyoung.llm_order_api.repository.OrderRepository;
import com.junyoung.llm_order_api.service.DistanceService;

/**
 * OrderControllerIntegrationTest
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "maps.provider=FAKE")
public class OrderControllerIntegrationTest {
    @Container
    static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:18");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @LocalServerPort
    int port;

    String getOrigin() {
        return "http://localhost:" + port;
    }

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OrderRepository orderRepository;

    @MockitoBean
    DistanceService distanceService;

    HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        when(distanceService.getDistance(
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble())).thenReturn(1000);
    }

    @Test
    void placeOrder_thenOrderAppearsInList() throws Exception {
        // given
        var restClient = RestClient.create(getOrigin());
        var orderRequest = new PlaceOrderRequest(
                List.of("22.3193", "114.1694"),
                List.of("22.3964", "114.1095"));

        // when calling placeOrder API
        var placeResponse = restClient
                .post()
                .uri("/orders")
                .body(orderRequest)
                .retrieve()
                .toEntity(String.class);
        var placedOrder = objectMapper.readTree(placeResponse.getBody());

        // then the order is added to the DB successfully
        assertThat(placeResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(placedOrder.get("id").asLong()).isPositive();
        assertThat(placedOrder.get("distance").asInt()).isEqualTo(1000);
        assertThat(placedOrder.get("status").asString())
                .isEqualTo("UNASSIGNED");

        // when calling getOrders API
        var listResponse = restClient
                .get()
                .uri("/orders?page=1&limit=10")
                .retrieve()
                .toEntity(String.class);

        // then the placed order is retrieved from the DB
        assertThat(listResponse.getStatusCode().is2xxSuccessful()).isTrue();

        var orders = objectMapper.readTree(listResponse.getBody());

        assertThat(orders.isArray()).isTrue();
        assertThat(orders.size()).isEqualTo(1);
        assertThat(orders.get(0).get("id").asLong())
                .isEqualTo(placedOrder.get("id").asLong());
        assertThat(orders.get(0).get("status").asString())
                .isEqualTo("UNASSIGNED");

        var takeResponse = restClient
                .patch()
                .uri("/orders/{id}", placedOrder.get("id").asLong())
                .body(new TakeOrderRequest("TAKEN"))
                .retrieve()
                .toEntity(String.class);

        assertThat(takeResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(objectMapper.readTree(takeResponse.getBody()).get("status").asString())
                .isEqualTo("SUCCESS");
    }

    @Test
    void takeOrder_twice_onlyFirstRequestSucceeds() throws Exception {
        // given
        var restClient = RestClient.create(getOrigin());
        var order = Order.create(
                22.3193,
                114.1694,
                22.3964,
                114.1095,
                1000);

        long orderId = orderRepository.saveAndFlush(order).getId();

        // when calling takeOrder API
        var takeResponse = restClient.patch()
                .uri("/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TakeOrderRequest("TAKEN"))
                .retrieve()
                .toEntity(String.class);

        assertThat(takeResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(objectMapper.readTree(takeResponse.getBody())
                .get("status").asString())
                .isEqualTo("SUCCESS");

        // when calling takeOrder API again
        assertThatThrownBy(() -> restClient.patch()
                .uri("/orders/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TakeOrderRequest("TAKEN"))
                .retrieve()
                .toEntity(String.class))
                .isInstanceOf(HttpClientErrorException.BadRequest.class)
                .satisfies(ex -> {
                    var body = objectMapper.readTree(((HttpClientErrorException) ex).getResponseBodyAsString());
                    assertThat(body.get("error").asString()).isEqualTo("ORDER_ALREADY_TAKEN");
                });

        // then the order is taken by the first request
        var orderOptional = orderRepository.findById(orderId);
        assertThat(orderOptional)
                .isPresent();
        assertThat(orderOptional.get().getStatus())
                .isEqualTo(OrderStatus.TAKEN);

    }

}