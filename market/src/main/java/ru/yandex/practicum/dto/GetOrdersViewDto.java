package ru.yandex.practicum.dto;

import java.io.Serializable;
import java.util.List;

public record GetOrdersViewDto(List<OrderDto> orders) implements Serializable {
}
