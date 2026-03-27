package ru.yandex.practicum.dto;

public record PagingDto(
        int pageNumber,
        int pageSize,
        boolean hasPrevious,
        boolean hasNext
) {}
