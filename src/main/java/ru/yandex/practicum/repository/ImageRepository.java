package ru.yandex.practicum.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Image;

public interface ImageRepository extends ReactiveCrudRepository<Image, Long> {
    Mono<Image> findByItemId(Long id);
}
