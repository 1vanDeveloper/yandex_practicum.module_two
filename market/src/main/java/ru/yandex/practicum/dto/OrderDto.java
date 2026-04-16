package ru.yandex.practicum.dto;

import java.util.List;

public record OrderDto(long id,
                       List<OrderItemDto> items) {
    public Double totalSum() {
        return items.stream().mapToDouble(i -> i.price() * i.count()).sum();
    }
}
