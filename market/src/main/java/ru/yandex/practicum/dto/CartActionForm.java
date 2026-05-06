package ru.yandex.practicum.dto;

import java.io.Serializable;

public record CartActionForm(
        long id,
        ActionDto action
) implements Serializable {
}
