package ru.yandex.practicum.dto;

import java.io.Serializable;
import java.util.List;

public record OrderDto(long id,
                       List<OrderItemDto> items) implements Serializable {
    public Double totalSum() {
        return items.stream().mapToDouble(i -> i.price() * i.count()).sum();
    }
}
