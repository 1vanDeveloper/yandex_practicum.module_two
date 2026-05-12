package ru.yandex.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.dto.SortDto;
import ru.yandex.practicum.service.ItemService;

@Controller
class ItemController {

    private final ItemService itemService;

    ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping({"/", "/items"})
    public Mono<Rendering> getItems(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "ALPHA") SortDto sort,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber) {

        return itemService.getItems(search, sort, pageSize, pageNumber)
                .map(viewData -> Rendering.view("items")
                        .modelAttribute("search", viewData.search())
                        .modelAttribute("sort", viewData.sort().name())
                        .modelAttribute("items", viewData.items())
                        .modelAttribute("paging", viewData.paging())
                        .build());
    }

    @GetMapping("/items/{id}")
    public Mono<Rendering> getItem(@PathVariable long id) {
        return itemService.getItem(id)
                .map(viewData -> Rendering.view("item")
                        .modelAttribute("item", viewData)
                        .build());
    }
}
