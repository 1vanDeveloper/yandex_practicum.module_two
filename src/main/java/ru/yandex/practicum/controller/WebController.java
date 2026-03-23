package ru.yandex.practicum.controller;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.ItemService;
import ru.yandex.practicum.service.OrderService;

import java.util.concurrent.CompletableFuture;

@Controller
class WebController {

    private final String userLogin = "user";
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;

    WebController(ItemService itemService, CartService cartService, OrderService orderService) {
        this.itemService = itemService;
        this.cartService = cartService;
        this.orderService = orderService;
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
    public CompletableFuture<String>  getItem(
            Model model,
            @PathVariable long id) {
        return CompletableFuture.supplyAsync(() -> {
            var viewData = itemService.getItemSync(id);
            model.addAttribute("item", viewData);
            return "item";
        });
    }

    @Async
    @GetMapping("/cart/items")
    public CompletableFuture<String> getCart(Model model) {
        return cartService.getUserCart(userLogin).thenApplyAsync(viewData -> {
            model.addAttribute("items", viewData.items());
            model.addAttribute("total", viewData.total());
            return "cart";
        });
    }

    @Async
    @GetMapping("/orders")
    public CompletableFuture<String> getOrders(Model model) {
        return orderService.getUserOrders(userLogin).thenApplyAsync(viewData -> {
            model.addAttribute("orders", viewData.orders());
            return "orders";
        });
    }

    @Async
    @GetMapping("/orders/{id}")
    public CompletableFuture<String>  getOrders(Model model,
                                                @PathVariable long id,
                                                @RequestParam(required = false) Boolean newOrder) {
        return orderService.getOrder(id).thenApplyAsync(viewData -> {
            model.addAttribute("order", viewData);
            return "order";
        });
    }
}
