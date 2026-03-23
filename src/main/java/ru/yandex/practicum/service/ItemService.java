package ru.yandex.practicum.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.GetItemsViewDto;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.dto.PagingDto;
import ru.yandex.practicum.dto.SortDto;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.ItemRepository;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис по управлению товаром
 */
public interface ItemService {
    @Async
    CompletableFuture<GetItemsViewDto> getItems(String search, SortDto sortOrder, Integer pageSize, Integer pageNumber);

    ItemDto getItemSync(long id);
}

@Service
class ImplementedItemService implements ItemService {

    private final ItemRepository itemRepository;

    public ImplementedItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public CompletableFuture<GetItemsViewDto> getItems(String search, SortDto sortOrder, Integer pageSize, Integer pageNumber) {
        return CompletableFuture.supplyAsync(() -> {
            var sort = switch (sortOrder) {
                case NO -> Sort.unsorted();
                case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
                case PRICE -> Sort.by(Sort.Direction.ASC, "price");
            };
            var pageParams = PageRequest.of(pageNumber, pageSize, sort);
            var page = (search == null || search.isEmpty())
                    ? itemRepository.findAll(pageParams)
                    : itemRepository.findByTitleContainingOrDescriptionContainingIgnoreCase(search, search, pageParams);
            return new GetItemsViewDto(
                    search,
                    sortOrder,
                    page.stream().map(ImplementedItemService::convert).toList(),
                    new PagingDto(
                            page.getNumber() + 1,
                            page.getSize(),
                            page.hasPrevious(),
                            page.hasNext()));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemSync(long id) {
        var item = itemRepository.getById(id);
        if (item == null) {
            throw new NoSuchElementException("id: " + id);
        }
        return convert(item);
    }

    private static ItemDto convert(Item item) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                "/images/" + item.getId(),
                item.getPrice().doubleValue(),
                item.getCount()
        );
    }
}