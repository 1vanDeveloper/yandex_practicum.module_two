package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Order;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    @Query("SELECT * FROM orders o WHERE o.user_id = (SELECT u.id FROM users u WHERE u.login = :login)")
    Flux<Order> findByUserLogin(String login);

    Mono<Order> findById(Long id);
}
