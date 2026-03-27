package ru.yandex.practicum.controller;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.SortDto;
import ru.yandex.practicum.service.ItemService;

import java.util.concurrent.CompletableFuture;

@Controller
class ItemController {

    private final ItemService itemService;

    ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Async
    @GetMapping({"/", "/items"})
    public CompletableFuture<String> getItems(
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALPHA") SortDto sort,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber) {

        return itemService.getItems(search, sort, pageSize, pageNumber - 1).thenApplyAsync(viewData -> {
            model.addAttribute("search", viewData.search());
            model.addAttribute("sort", viewData.sort().name());
            model.addAttribute("items", viewData.items());
            model.addAttribute("paging", viewData.paging());
            return "items";
        });
    }

    @Async
    @GetMapping("/items/{id}")
    public CompletableFuture<String> getItem(
            Model model,
            @PathVariable long id) {
        return CompletableFuture.supplyAsync(() -> {
            var viewData = itemService.getItemSync(id);
            model.addAttribute("item", viewData);
            return "item";
        });
    }
}
