package ru.yandex.practicum.dto;

public record OrderItemDto(String title,
                           int count,
                           double price) { }
