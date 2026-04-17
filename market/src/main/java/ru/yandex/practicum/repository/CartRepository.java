package ru.yandex.practicum.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Cart;

public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {
    @Query("SELECT c.* FROM carts c JOIN users u ON c.user_id = u.id WHERE u.login = :login")
    Mono<Cart> findByUserLogin(String login);
}
