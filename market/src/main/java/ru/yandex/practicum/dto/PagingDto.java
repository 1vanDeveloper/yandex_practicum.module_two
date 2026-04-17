package ru.yandex.practicum.dto;

import java.io.Serializable;

public record PagingDto(
        int pageNumber,
        int pageSize,
        boolean hasPrevious,
        boolean hasNext
) implements Serializable {}
