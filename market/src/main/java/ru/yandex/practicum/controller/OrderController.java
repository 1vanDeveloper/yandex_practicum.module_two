package ru.yandex.practicum.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.security.SecurityUser;
import ru.yandex.practicum.service.OrderService;

@Controller
class OrderController {

    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public Mono<Rendering> getOrders(@AuthenticationPrincipal SecurityUser currentUser) {
        return orderService.getUserOrders(currentUser.getUsername())
                .map(viewData -> Rendering.view("orders")
                        .modelAttribute("orders", viewData.orders())
                        .build());
    }

    @GetMapping("/orders/{id}")
    public Mono<Rendering> getOrder(
            @PathVariable long id,
            @RequestParam(required = false) Boolean newOrder) {
        return orderService.getOrder(id)
                .map(viewData -> Rendering.view("order")
                        .modelAttribute("order", viewData)
                        .modelAttribute("newOrder", newOrder)
                        .build());
    }

    @PostMapping("/buy")
    public Mono<Rendering> buy(@AuthenticationPrincipal SecurityUser currentUser) {
        return orderService.createOrder(currentUser.getUsername())
                .map(id -> Rendering.redirectTo("/orders/" + id + "?newOrder=true").build())
                .doOnError(e -> {
                    System.err.println("ОШИБКА ПРИ СОЗДАНИИ ЗАКАЗА: " + e.getMessage());
                })
                .onErrorResume(e -> Mono.just(Rendering.view("error").build()));
    }
}
