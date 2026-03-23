package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByTitleContainingOrDescriptionContainingIgnoreCase(
            String searchTitle,
            String searchDescription,
            Pageable pageable
    );

    @Query("SELECT i FROM Item i WHERE i.id = :id")
    Item getById(@Param("id") Long id);
}
