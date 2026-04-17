package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.dto.ActionDto;
import ru.yandex.practicum.dto.GetCartViewDto;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.model.Cart;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.CartItemRepository;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.UserRepository;

import java.util.Comparator;
import java.util.NoSuchElementException;

public interface CartService {
    Mono<GetCartViewDto> getUserCart(String login);

    Mono<Void> editCountItemCart(String login, long itemId, ActionDto action);
}

@Service
class ImplementedCartService implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    ImplementedCartService(CartRepository cartRepository, CartItemRepository cartItemRepository, 
                          ItemRepository itemRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<GetCartViewDto> getUserCart(String login) {
        return getOrCreateCart(login)
                .flatMapMany(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                                .map(item -> convert(cartItem, item))))
                .sort(Comparator.comparing(ItemDto::title))
                .collectList()
                .map(GetCartViewDto::new);
    }

    @Override
    public Mono<Void> editCountItemCart(String login, long itemId, ActionDto action) {
        return getOrCreateCart(login)
                .flatMap(cart -> editCountItemCartSync(cart, itemId, action).then());
    }

    private Mono<Boolean> editCountItemCartSync(Cart cart, long itemId, ActionDto action) {
        return cartItemRepository.findByItemIdAndCartId(itemId, cart.getId())
                .flatMap(cartItem -> processExistingItem(cartItem, action).thenReturn(true))
                .switchIfEmpty(Mono.defer(() -> {
                    if (action == ActionDto.PLUS) {
                        return itemRepository.findById(itemId)
                                .switchIfEmpty(Mono.error(new NoSuchElementException("id: " + itemId)))
                                .flatMap(item -> {
                                    var cartItem = new CartItem();
                                    cartItem.setCartId(cart.getId());
                                    cartItem.setItemId(item.getId());
                                    cartItem.setCount(1);
                                    return cartItemRepository.save(cartItem).then();
                                });
                    } else {
                        return Mono.empty();
                    }
                }).thenReturn(true));
    }

    private Mono<Void> processExistingItem(CartItem cartItem, ActionDto action) {
        return itemRepository.findById(cartItem.getItemId())
                .flatMap(item -> {
                    if (item.getCount() > cartItem.getCount() && action == ActionDto.PLUS) {
                        cartItem.setCount(cartItem.getCount() + 1);
                    } else if (cartItem.getCount() > 1 && action == ActionDto.MINUS) {
                        cartItem.setCount(cartItem.getCount() - 1);
                    } else if (item.getCount() < cartItem.getCount()) {
                        cartItem.setCount(item.getCount());
                    } else if (cartItem.getCount() == 1 && action == ActionDto.MINUS) {
                        return cartItemRepository.delete(cartItem).then();
                    } else if (action == ActionDto.DELETE) {
                        return cartItemRepository.delete(cartItem).then();
                    }
                    return cartItemRepository.save(cartItem).then();
                });
    }

    private Mono<Cart> getOrCreateCart(String login) {
        return cartRepository.findByUserLogin(login)
                .switchIfEmpty(
                        userRepository.findByLogin(login)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(login)))
                                .flatMap(user -> {
                                    var newCart = new Cart();
                                    newCart.setUserId(user.getId());
                                    return cartRepository.save(newCart);
                                })
                );
    }

    private static ItemDto convert(CartItem cartItem, Item item) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                "images/" + item.getId(),
                item.getPrice().doubleValue(),
                cartItem.getCount()
        );
    }
}
