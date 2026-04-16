package ru.yandex.practicum.service;

import jakarta.validation.ValidationException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.client.api.PaymentsApi;
import ru.yandex.practicum.client.model.CreatePaymentRequest;
import ru.yandex.practicum.dto.GetOrdersViewDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.OrderItemDto;
import ru.yandex.practicum.model.*;
import ru.yandex.practicum.repository.CartItemRepository;
import ru.yandex.practicum.repository.CartRepository;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.repository.OrderItemRepository;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

public interface OrderService {
    Mono<GetOrdersViewDto> getUserOrders(String login);
    Mono<OrderDto> getOrder(long id);
    Mono<Long> createOrder(String userLogin);
}

@Service
class ImplementedOrderService implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final PaymentsApi paymentsApi;

    ImplementedOrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                           CartRepository cartRepository, CartItemRepository cartItemRepository,
                           ItemRepository itemRepository, PaymentsApi paymentsApi) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
        this.paymentsApi = paymentsApi;
    }

    @Override
    public Mono<GetOrdersViewDto> getUserOrders(String login) {
        return orderRepository.findByUserLogin(login)
                .flatMap(this::getOrderDto)
                .collectList()
                .map(GetOrdersViewDto::new);
    }

    @Override
    public Mono<OrderDto> getOrder(long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("id: " + id)))
                .flatMap(this::getOrderDto);
    }

    private Mono<OrderDto> getOrderDto(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .map(ImplementedOrderService::convert)
                .collectList()
                .map(items -> new OrderDto(order.getId(), items));
    }

    @Override
    @Transactional
    public Mono<Long> createOrder(String userLogin) {
        return cartRepository.findByUserLogin(userLogin)
                .switchIfEmpty(Mono.error(new NoSuchElementException("cart is not found")))
                .flatMap(this::createOrderSync)
                .doOnNext(id -> System.out.println("DEBUG: Order created: " + id))
                .doOnError(e -> System.err.println("DEBUG: ERROR in service: " + e.getMessage()));
    }

    private Mono<Long> createOrderSync(Cart cart) {
        return cartItemRepository.findByCartId(cart.getId())
                .collectList()
                .switchIfEmpty(Mono.error(new NoSuchElementException("cart items is not found")))
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.error(new ValidationException("cart is empty"));
                    }
                    return Flux.fromIterable(cartItems)
                            .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                                    .switchIfEmpty(Mono.error(new NoSuchElementException("Item not found: " + cartItem.getItemId())))
                                    .map(item ->
                                    {
                                        if (item.getCount() < cartItem.getCount()) {
                                            throw new ValidationException("Not enough items in stock");
                                        }
                                        item.setCount(item.getCount() - cartItem.getCount());

                                        var orderItem = new OrderItem();
                                        orderItem.setItemId(item.getId());
                                        orderItem.setTitle(item.getTitle());
                                        orderItem.setPrice(item.getPrice());
                                        orderItem.setCount(cartItem.getCount());
                                        return Pair.of(orderItem, item);
                                    }))
                            .collectList()
                            .flatMap(pairs -> {
                                var newOrder = new Order();
                                newOrder.setUserId(cart.getUserId());
                                return orderRepository.save(newOrder)
                                        .flatMap(savedOrder -> {
                                            List<OrderItem> orderItems = pairs.stream()
                                                    .map(p -> {
                                                        p.getFirst().setOrderId(savedOrder.getId());
                                                        return p.getFirst();
                                                    }).toList();
                                            List<Item> itemsToUpdate = pairs.stream().map(Pair::getSecond).toList();

                                            BigDecimal totalAmount = pairs.stream()
                                                    .map(p -> p.getFirst().getPrice().multiply(BigDecimal.valueOf(p.getFirst().getCount())))
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                                            CreatePaymentRequest paymentRequest = new CreatePaymentRequest()
                                                    .orderId(String.valueOf(savedOrder.getId()))
                                                    .amount(totalAmount)
                                                    .currency("RUB");

                                            return orderItemRepository.saveAll(orderItems).collectList()
                                                    .flatMap(i -> itemRepository.saveAll(itemsToUpdate).collectList())
                                                    .flatMap(i -> paymentsApi.createPayment(paymentRequest))
                                                    .flatMap(i -> cartItemRepository.deleteAll(cartItems))
                                                    .then(Mono.defer(() -> cartRepository.delete(cart)))
                                                    .thenReturn(savedOrder.getId());
                                        });
                            });
                });
    }

    private static OrderItemDto convert(OrderItem orderItem) {
        return new OrderItemDto(orderItem.getTitle(), orderItem.getCount(), orderItem.getPrice().doubleValue());
    }
}
