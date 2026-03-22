package ru.yandex.practicum.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public interface OrderService {
    @Async
    CompletableFuture<List<Order>> getUserOrders(String login);
    @Async
    CompletableFuture<Order> getOrder(long id);
}

@Service
class ImplementedOrderService implements OrderService {

    private final OrderRepository orderRepository;

    ImplementedOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public CompletableFuture<List<Order>> getUserOrders(String login) {
        return CompletableFuture.supplyAsync(() -> orderRepository.getOrdersByUserLogin(login));
    }

    @Override
    public CompletableFuture<Order> getOrder(long id) {
        return CompletableFuture.supplyAsync(() -> {
            var order = orderRepository.getOrderById(id);
            if (order.isEmpty()) {
                throw new NoSuchElementException("id: " + id);
            }

            return order.get();
        });
    }
}
