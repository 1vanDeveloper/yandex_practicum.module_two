package ru.yandex.practicum.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.GetCartViewDto;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.model.Cart;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.UserRepository;

import java.util.concurrent.CompletableFuture;

public interface CartService {
    @Async
    CompletableFuture<GetCartViewDto> getUserCart(String login);
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
    public CompletableFuture<GetCartViewDto> getUserCart(String login) {
        return CompletableFuture.supplyAsync(() -> {
            var cart  = getCart(login);
            return new GetCartViewDto(
                    cart.getCartItems()
                            .stream()
                            .map(ImplementedCartService::convert)
                            .toList());
        });
    }

    private Cart getCart(String login) {
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
    }

    private static ItemDto convert(CartItem cartItem) {
        return new ItemDto(
                cartItem.getId(),
                cartItem.getItem().getTitle(),
                cartItem.getItem().getDescription(),
                "/images/" + cartItem.getItem().getId(),
                cartItem.getItem().getPrice().doubleValue(),
                cartItem.getCount()
        );
    }
}
