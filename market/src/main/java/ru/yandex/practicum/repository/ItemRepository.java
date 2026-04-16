package ru.yandex.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Item;

public interface ItemRepository extends R2dbcRepository<Item, Long> {
    Flux<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description, Pageable pageable);

    Flux<Item> findAllBy(Pageable pageable);

    Mono<Long> countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description);
}
