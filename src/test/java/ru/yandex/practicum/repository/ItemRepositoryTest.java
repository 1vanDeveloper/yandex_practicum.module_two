package ru.yandex.practicum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.yandex.practicum.TestConfig;
import ru.yandex.practicum.model.Item;

import java.util.Comparator;

@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findAll() {
        StepVerifier.create(itemRepository.findAll().sort(Comparator.comparing(Item::getId)))
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void findById() {
        StepVerifier.create(itemRepository.findById(1L))
                .expectNextMatches(item -> item.getTitle().equals("Title 1 first"))
                .verifyComplete();
    }
}
