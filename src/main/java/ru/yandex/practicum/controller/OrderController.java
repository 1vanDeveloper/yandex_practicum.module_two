package ru.yandex.practicum.controller;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.service.OrderService;

import java.util.concurrent.CompletableFuture;

@Controller
class OrderController {

    private final String userLogin = "user";
    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
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
    public CompletableFuture<String> getOrders(Model model,
                                                @PathVariable long id,
                                                @RequestParam(required = false) Boolean newOrder) {
        return orderService.getOrder(id).thenApplyAsync(viewData -> {
            model.addAttribute("order", viewData);
            return "order";
        });
    }

    @Async
    @PostMapping("buy")
    public CompletableFuture<String> buy() {
        return orderService.createOrder(userLogin).thenApplyAsync(id -> UriComponentsBuilder.fromPath("redirect:/orders/" + id)
                .queryParam("newOrder", true)
                .toUriString());
    }
}
