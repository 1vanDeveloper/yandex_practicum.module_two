package ru.yandex.practicum.dto;

public record ItemDto(
        long id,
        String title,
        String description,
        String imgPath,
        double price,
        int count
) {}
