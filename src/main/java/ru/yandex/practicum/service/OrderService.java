package ru.yandex.practicum.service;

import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.dto.GetOrdersViewDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.OrderItemDto;
import ru.yandex.practicum.model.Cart;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItem;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public interface OrderService {
    @Async
    CompletableFuture<GetOrdersViewDto> getUserOrders(String login);
    @Async
    CompletableFuture<OrderDto> getOrder(long id);
    @Async
    CompletableFuture<Long> createOrder(String userLogin);
}

@Service
class ImplementedOrderService implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    ImplementedOrderService(OrderRepository orderRepository, CartRepository cartRepository, EntityManager entityManager, TransactionTemplate transactionTemplate) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public CompletableFuture<GetOrdersViewDto> getUserOrders(String login) {
        return CompletableFuture.supplyAsync(() -> {
            var orders = orderRepository.getOrdersByUserLogin(login);
            return  new GetOrdersViewDto(
                    orders.stream().map(ImplementedOrderService::convert).toList()
            );
        });
    }

    @Override
    public CompletableFuture<OrderDto> getOrder(long id) {
        return CompletableFuture.supplyAsync(() -> {
            var order = orderRepository.getOrderById(id);
            if (order.isEmpty()) {
                throw new NoSuchElementException("id: " + id);
            }

            return convert(order.get());
        });
    }

    @Override
    public CompletableFuture<Long> createOrder(String userLogin) {
        return CompletableFuture.supplyAsync(() ->
                transactionTemplate.execute(status -> {
                    try {
                        return createOrderSync(userLogin);
                    } catch (ValidationException e) {
                        throw new RuntimeException(e);
                    }
                }));
    }

    private Long createOrderSync(String userLogin) throws ValidationException {
        var optCart = cartRepository.getCartByUserLogin(userLogin);
        if (optCart.isEmpty()) {
            throw new NoSuchElementException("cart is not found");
        }

        var cart = optCart.get();
        ValidateCart(cart);
        var newOrder = new Order();
        newOrder.setUser(cart.getUser());
        var orderItems = new LinkedHashSet<OrderItem>();
        for (var cartItem : cart.getCartItems()) {
            var orderItem = new OrderItem();
            orderItem.setItem(cartItem.getItem());
            orderItem.setTitle(cartItem.getItem().getTitle());
            orderItem.setPrice(cartItem.getItem().getPrice());
            orderItem.setCount(cartItem.getCount());
            orderItem.setOrder(newOrder);
            orderItems.add(orderItem);
            entityManager.persist(orderItem);

            var item = cartItem.getItem();
            item.setCount(item.getCount() - orderItem.getCount());
            entityManager.merge(item);

            cartItem = entityManager.merge(cartItem);
            entityManager.remove(cartItem);
        }
        newOrder.setOrderItems(orderItems);
        entityManager.persist(newOrder);

        cart = entityManager.merge(cart);
        entityManager.remove(cart);

        return newOrder.getId();
    }

    private static OrderDto convert(Order order) {
        return new OrderDto(order.getId(),
                order.getOrderItems()
                        .stream()
                        .map(ImplementedOrderService::convert)
                        .toList());
    }

    private static OrderItemDto convert(OrderItem orderItem) {
        return new OrderItemDto(orderItem.getTitle(), orderItem.getCount(), orderItem.getPrice().doubleValue());
    }

    private static void ValidateCart(Cart cart) throws ValidationException {
        if (cart.getCartItems().isEmpty()) {
            throw new ValidationException("cart is empty");
        }

        for (var item : cart.getCartItems()) {
            if (item.getItem().getCount() < item.getCount()) {
                throw new ValidationException("item " + item.getItem().getTitle() + " is " + item.getItem().getCount() + " count only");
            }
        }
    }
}
