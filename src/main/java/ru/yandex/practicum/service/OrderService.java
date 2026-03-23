package ru.yandex.practicum.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.GetOrdersViewDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.OrderItemDto;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItem;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public interface OrderService {
    @Async
    CompletableFuture<GetOrdersViewDto> getUserOrders(String login);
    @Async
    CompletableFuture<OrderDto> getOrder(long id);
}

@Service
class ImplementedOrderService implements OrderService {

    private final OrderRepository orderRepository;

    ImplementedOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
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
}
