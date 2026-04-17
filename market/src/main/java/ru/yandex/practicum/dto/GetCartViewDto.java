package ru.yandex.practicum.dto;

import java.util.List;

public record GetCartViewDto(List<ItemDto> items) {
    public double total() {
        return items.stream().mapToDouble(i -> i.price() * i.count()).sum();
    }
}
