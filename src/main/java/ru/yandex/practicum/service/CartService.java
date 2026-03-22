package ru.yandex.practicum.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Cart;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.UserRepository;

import java.util.concurrent.CompletableFuture;

public interface CartService {
    @Async
    CompletableFuture<Cart> getUserCart(String login);
}

@Service
class ImplementedCartService implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    ImplementedCartService(CartRepository cartRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CompletableFuture<Cart> getUserCart(String login) {
        return CompletableFuture.supplyAsync(() -> {
            var cart = cartRepository.getCartByUserLogin(login);
            if (cart.isPresent()) {
                return cart.get();
            }

            var user = userRepository.getUserByLogin(login);
            if (user.isEmpty()) {
                throw new IllegalArgumentException(login);
            }
            var newCart = new Cart();
            newCart.setUser(user.get());
            return newCart;
        });
    }
}
