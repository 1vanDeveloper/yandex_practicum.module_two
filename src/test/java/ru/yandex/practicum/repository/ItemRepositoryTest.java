package ru.yandex.practicum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findByTitleContainingOrDescriptionContainingIgnoreCase() {
        var pageParams = PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "title"));
        var items = itemRepository.findByTitleContainingOrDescriptionContainingIgnoreCase("3 ", "3 ", pageParams);

        assertEquals(1, items.getTotalElements());
        var optItem = items.get().findFirst();
        assertTrue(optItem.isPresent());
        var item = optItem.get();
        assertEquals("Title 3 third", item.getTitle());
    }
}