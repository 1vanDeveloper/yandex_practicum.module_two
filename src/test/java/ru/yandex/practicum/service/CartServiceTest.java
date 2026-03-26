package ru.yandex.practicum.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.TestConfig;
import ru.yandex.practicum.dto.ActionDto;
import ru.yandex.practicum.model.CartItem;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void editCountItemCart_shouldIncrease() {
        // Arrange
        var cartItemId = 1;
        var cartItem =  entityManager.find(CartItem.class, cartItemId);
        var itemCount = cartItem.getCount();

        // Act
        cartService.editCountItemCart("user", 1, ActionDto.PLUS).join();

        // Assert
        entityManager.clear();
        var editedCartItem = entityManager.find(CartItem.class, cartItemId);
        var editedItemCount = editedCartItem.getCount();

        assertEquals(itemCount + 1, editedItemCount);
    }
}