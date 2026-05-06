package ru.yandex.practicum.dto;

import java.io.Serializable;

public record ItemsActionForm(
        long id,
        String search,
        SortDto sort,
        Integer pageSize,
        Integer pageNumber,
        ActionDto action
) implements Serializable {

    public ItemsActionForm {
        if (search == null) {
            search = "";
        }
        if (sort == null) {
            sort = SortDto.ALPHA;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        if (pageNumber == null) {
            pageNumber = 1;
        }
    }
}
