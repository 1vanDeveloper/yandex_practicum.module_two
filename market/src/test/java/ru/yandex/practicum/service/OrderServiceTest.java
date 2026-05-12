package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.yandex.practicum.TestConfig;

import java.util.NoSuchElementException;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    void getUserOrders_existingUser_returnsOrders() {
        StepVerifier.create(orderService.getUserOrders("user"))
                .expectNextMatches(orders -> orders.orders() != null)
                .verifyComplete();
    }

    @Test
    void getOrder_existingId_returnsOrder() {
        StepVerifier.create(orderService.getOrder(1L))
                .expectNextMatches(order -> order.id() == 1L)
                .verifyComplete();
    }

    @Test
    void getOrder_nonExistingId_returnsError() {
        StepVerifier.create(orderService.getOrder(999L))
                .expectErrorMatches(throwable -> throwable instanceof NoSuchElementException)
                .verify();
    }

    @Test
    void createOrder_emptyCart_returnsError() {
        StepVerifier.create(orderService.createOrder("empty_cart_user"))
                .expectErrorMatches(throwable ->
                    throwable instanceof java.util.NoSuchElementException ||
                    throwable.getMessage().contains("cart"))
                .verify();
    }

    @Test
    void createOrder_fromCart_returnsErrorWhenNoItems() {
        StepVerifier.create(orderService.createOrder("user"))
                .expectErrorMatches(throwable ->
                    throwable.getMessage().contains("cart is empty") ||
                    throwable.getMessage().contains("cart items is not found"))
                .verify();
    }
}
