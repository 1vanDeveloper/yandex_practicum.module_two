package ru.yandex.practicum.service;

import jakarta.persistence.EntityManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.dto.ActionDto;
import ru.yandex.practicum.dto.GetCartViewDto;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.model.Cart;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.UserRepository;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public interface CartService {
    @Async
    CompletableFuture<GetCartViewDto> getUserCart(String login);

    @Async
    CompletableFuture<Void> editCountItemCart(String login, long itemId, ActionDto action);
}

@Service
class ImplementedCartService implements CartService {

    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    ImplementedCartService(CartRepository cartRepository, ItemRepository itemRepository, UserRepository userRepository, EntityManager entityManager, TransactionTemplate transactionTemplate) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public CompletableFuture<GetCartViewDto> getUserCart(String login) {
        return CompletableFuture.supplyAsync(() -> {
            var cart  = getCart(login);
            return new GetCartViewDto(
                    cart.getCartItems()
                            .stream()
                            .map(ImplementedCartService::convert)
                            .sorted(Comparator.comparing(ItemDto::title))
                            .toList());
        });
    }

    @Override
    public CompletableFuture<Void> editCountItemCart(String login, long itemId, ActionDto action) {
        return CompletableFuture.supplyAsync(() ->
                transactionTemplate.execute(_ -> {
                    editCountItemCartSync(login, itemId, action);
                    return null;
        }));
    }

    private void editCountItemCartSync(String login, long itemId, ActionDto action) {
        entityManager.clear();
        var cart = getCart(login);
        var existsItem = cart.getCartItems()
                .stream()
                .filter(i -> i.getId() == itemId)
                .findFirst();
        CartItem cartItem;
        if (existsItem.isPresent()) {
            cartItem = existsItem.get();
        } else if (action == ActionDto.PLUS) {
            var item = itemRepository.getById(itemId);
            if (item == null) {
                throw new NoSuchElementException("id: " + itemId);
            }
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setItem(item);
            cartItem.setCount(0);
        } else {
            return;
        }

        if (cartItem.getId() == null) {
            entityManager.persist(cartItem);
        } else {
            cartItem = entityManager.merge(cartItem);
        }

        if (cartItem.getItem().getCount() > cartItem.getCount() && action == ActionDto.PLUS) { // если добавляем доступное число
            cartItem.setCount(cartItem.getCount() + 1);
        } else if (cartItem.getCount() > 1 && action == ActionDto.MINUS) { // если убираем доступное число
            cartItem.setCount(cartItem.getCount() - 1);
        } else if (cartItem.getItem().getCount() < cartItem.getCount()) { // если превисили лимит
            cartItem.setCount(cartItem.getItem().getCount());
        } else if (cartItem.getCount() == 1 && action == ActionDto.MINUS) { // если убираем последний
            entityManager.remove(cartItem);
        } else if (action == ActionDto.DELETE) {
            entityManager.remove(cartItem);
        }
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
        entityManager.persist(newCart);
        return newCart;
    }

    private static ItemDto convert(CartItem cartItem) {
        return new ItemDto(
                cartItem.getId(),
                cartItem.getItem().getTitle(),
                cartItem.getItem().getDescription(),
                "images/" + cartItem.getItem().getId(),
                cartItem.getItem().getPrice().doubleValue(),
                cartItem.getCount()
        );
    }
}
