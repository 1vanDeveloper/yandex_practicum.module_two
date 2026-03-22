package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.ItemRepository;
import ru.yandex.practicum.service.model.SortOrder;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис по управлению товаром
 */
public interface ItemService {
    @Async
    CompletableFuture<Page<Item>> getItems(String search, SortOrder sortOrder, Integer pageSize, Integer pageNumber);
    @Async
    CompletableFuture<Optional<Item>> getItem(long id);
}

@Service
class ImplementedItemService implements ItemService {

    private final ItemRepository itemRepository;

    public ImplementedItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public CompletableFuture<Page<Item>> getItems(String search, SortOrder sortOrder, Integer pageSize, Integer pageNumber) {
        return CompletableFuture.supplyAsync(() -> {
            var sort = switch (sortOrder) {
                case NO -> Sort.unsorted();
                case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
                case PRICE -> Sort.by(Sort.Direction.ASC, "price");
            };
            var pageParams = PageRequest.of(pageNumber, pageSize, sort);
            return itemRepository.findByTitleContainingOrDescriptionContainingIgnoreCaseOrder(search, search, pageParams);
        });
    }

    @Override
    public CompletableFuture<Optional<Item>> getItem(long id) {
        return CompletableFuture.supplyAsync(() -> itemRepository.getItemById(id));
    }
}