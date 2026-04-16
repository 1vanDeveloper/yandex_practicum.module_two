package ru.yandex.practicum.dto;

import java.util.List;

public record GetItemsViewDto(
        String search,
        SortDto sort,
        List<ItemDto> items,
        PagingDto paging) {}
