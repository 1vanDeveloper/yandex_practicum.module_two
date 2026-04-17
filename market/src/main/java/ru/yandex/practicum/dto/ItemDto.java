package ru.yandex.practicum.dto;

import java.io.Serializable;

public record ItemDto(
        long id,
        String title,
        String description,
        String imgPath,
        double price,
        int count
) implements Serializable {}
