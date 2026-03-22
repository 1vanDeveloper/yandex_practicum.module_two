package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.Item;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByTitleContainingOrDescriptionContainingIgnoreCaseOrder(
            String searchTitle,
            String searchDescription,
            Pageable pageable
    );

    Optional<Item> getItemById(Long id);
}
