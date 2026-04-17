package ru.yandex.practicum.dto;

import java.io.Serializable;

public record OrderItemDto(String title,
                           int count,
                           double price) implements Serializable { }
