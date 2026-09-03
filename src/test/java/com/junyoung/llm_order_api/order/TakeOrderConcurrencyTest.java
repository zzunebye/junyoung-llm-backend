package com.junyoung.llm_order_api.order;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.junyoung.llm_order_api.exceptions.BusinessException;
import com.junyoung.llm_order_api.exceptions.ErrorCode;
import com.junyoung.llm_order_api.order.dto.TakeOrderRequest;
import com.junyoung.llm_order_api.order.entity.Order;
import com.junyoung.llm_order_api.order.entity.OrderStatus;

/// Additional test for testing concurrency handling of the takeOrder method
@SpringBootTest
@TestPropertySource(properties = {
        "maps.provider=FAKE",
        "maps.api-key=test"
})
class TakeOrderConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(TakeOrderConcurrencyTest.class);

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void takeOrder_concurrentRequests_onlyOneSucceeds() throws Exception {
        // create a test order
        var order = Order.create(22.3193, 114.1694, 22.3964, 114.1095, 1000);
        Long orderId = orderRepository.saveAndFlush(order).getId();

        // prepare a latch for synchronization
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(2);

        // counters for success/failure
        var successes = new AtomicInteger();
        var alreadyTaken = new AtomicInteger();

        // define the task to try to take the order
        Runnable take = () -> {
            try {
                start.await(); // wait for the start signal
                orderService.takeOrder(new TakeOrderRequest("TAKEN"), orderId); // take the order
                successes.incrementAndGet(); // increment the success counter
                // log the success result: order.status
                var currentOrder = orderRepository.findById(orderId).orElse(null);
                log.info("Success, current status: {}", currentOrder != null ? currentOrder.getStatus() : null);
            } catch (BusinessException e) {
                if (e.getErrorCode() == ErrorCode.ORDER_ALREADY_TAKEN) {
                    alreadyTaken.incrementAndGet(); // increment the already taken counter
                    // log the failure result: order.status
                    var currentOrder = orderRepository.findById(orderId).orElse(null);
                    log.info("Failed(already taken), current status: {}",
                            currentOrder != null ? currentOrder.getStatus() : null);
                } else {
                    // log other business exceptions
                    log.error("Failed(business exception): {}", e.toString(), e);
                    throw e; // throw other business exceptions
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // set the interrupt status on the current thread
                log.warn("Interrupted while trying to take order", e);
            } finally {
                done.countDown(); // signal the completion of the task
            }
        };

        // try to take the order concurrently with 2 threads
        var pool = Executors.newFixedThreadPool(2);
        pool.submit(take);
        pool.submit(take);
        start.countDown(); // start the concurrent tasks
        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue(); // check if the tasks completed within 5 seconds
        pool.shutdown();

        // log the final result: order.status
        var finalOrder = orderRepository.findById(orderId).orElse(null);
        log.info("Final order status: {}", finalOrder != null ? finalOrder.getStatus() : null);

        // verify that one is successful and one is failed (already taken)
        assertThat(successes.get()).isEqualTo(1);
        assertThat(alreadyTaken.get()).isEqualTo(1);
        // check if the order status is TAKEN
        assertThat(finalOrder != null ? finalOrder.getStatus() : null)
                .isEqualTo(OrderStatus.TAKEN);
    }
}