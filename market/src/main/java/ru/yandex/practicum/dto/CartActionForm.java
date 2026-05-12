package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Min;

import java.io.Serializable;

public record CartActionForm(
        @Min(1) long id,
        ActionDto action
) implements Serializable {
}
