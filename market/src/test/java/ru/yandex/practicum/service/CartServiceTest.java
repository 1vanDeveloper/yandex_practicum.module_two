package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.yandex.practicum.TestConfig;
import ru.yandex.practicum.dto.ActionDto;

import java.util.NoSuchElementException;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Test
    void getUserCart_existingUser_returnsCartWithItems() {
        StepVerifier.create(cartService.getUserCart("user"))
                .expectNextMatches(cart -> cart.items() != null)
                .verifyComplete();
    }

    @Test
    void getUserCart_newUser_throwsError() {
        StepVerifier.create(cartService.getUserCart("new_user"))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException)
                .verify();
    }

    @Test
    void editCountItemCart_plusAction_incrementsCount() {
        StepVerifier.create(cartService.editCountItemCart("user", 1L, ActionDto.PLUS))
                .verifyComplete();
    }

    @Test
    void editCountItemCart_minusAction_decrementsCount() {
        StepVerifier.create(cartService.editCountItemCart("user", 1L, ActionDto.MINUS))
                .verifyComplete();
    }

    @Test
    void editCountItemCart_deleteAction_removesItem() {
        StepVerifier.create(cartService.editCountItemCart("user", 1L, ActionDto.DELETE))
                .verifyComplete();
    }

    @Test
    void editCountItemCart_nonExistingItem_throwsError() {
        StepVerifier.create(cartService.editCountItemCart("user", 999L, ActionDto.PLUS))
                .expectErrorMatches(throwable -> throwable instanceof NoSuchElementException)
                .verify();
    }
}
