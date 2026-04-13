package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.yandex.practicum.TestConfig;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Test
    void getUserCart() {
        StepVerifier.create(cartService.getUserCart("user"))
                .expectNextMatches(cart -> cart.items() != null)
                .verifyComplete();
    }
}
