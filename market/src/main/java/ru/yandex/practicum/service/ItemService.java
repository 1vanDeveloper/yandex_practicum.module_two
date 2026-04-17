package ru.yandex.practicum.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.dto.GetItemsViewDto;
import ru.yandex.practicum.dto.ItemDto;
import ru.yandex.practicum.dto.PagingDto;
import ru.yandex.practicum.dto.SortDto;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.repository.ItemRepository;

import java.util.List;
import java.util.NoSuchElementException;


/**
 * Сервис по управлению товаром
 */
public interface ItemService {
    Mono<GetItemsViewDto> getItems(String search, SortDto sortOrder, Integer pageSize, Integer pageNumber);

    Mono<ItemDto> getItem(long id);
}

@Service
class ImplementedItemService implements ItemService {

    private final ItemRepository itemRepository;

    public ImplementedItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    @Cacheable(value = {"items-list"}, key = "#search + ':' + #sortOrder + ':' + #pageSize + ':' + #pageNumber")
    public Mono<GetItemsViewDto> getItems(String search, SortDto sortOrder, Integer pageSize, Integer pageNumber) {
        var sort = switch (sortOrder) {
            case NO -> Sort.unsorted();
            case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
        };

        var pageParams = PageRequest.of(pageNumber - 1, pageSize, sort);
        boolean isSearchEmpty = (search == null || search.isBlank());

        Flux<Item> itemsFlux = isSearchEmpty
                ? itemRepository.findAllBy(pageParams)
                : itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageParams);

        Mono<Long> countMono = isSearchEmpty
                ? itemRepository.count()
                : itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search);

        return Mono.zip(itemsFlux.collectList(), countMono)
                .map(tuple -> {
                    List<Item> items = tuple.getT1();
                    long totalCount = tuple.getT2();

                    long totalPages = (long) Math.ceil((double) totalCount / pageSize);
                    boolean hasNext = pageNumber < totalPages;
                    boolean hasPrevious = pageNumber > 1;

                    return new GetItemsViewDto(
                            search,
                            sortOrder,
                            items.stream().map(ImplementedItemService::convert).toList(),
                            new PagingDto(pageNumber, pageSize, hasPrevious, hasNext)
                    );
                });
    }

    @Override
    @Cacheable(value = {"items"}, key = "#id")
    public Mono<ItemDto> getItem(long id) {
        return itemRepository.findById(id)
                .map(ImplementedItemService::convert)
                .switchIfEmpty(Mono.error(new NoSuchElementException("id: " + id)));
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
