package ru.yandex.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.dto.ActionDto;
import ru.yandex.practicum.dto.SortDto;
import ru.yandex.practicum.service.CartService;

@Controller
class CartController {

    private final String userLogin = "user";
    private final CartService cartService;

    CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public Mono<Rendering> editCartItemsFromItems(
            @RequestParam long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALPHA") SortDto sort,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam ActionDto action) {

        return cartService.editCountItemCart(userLogin, id, action)
                .thenReturn(Rendering.redirectTo("/items?search=" + search + "&sort=" + sort + "&pageNumber=" + pageNumber + "&pageSize=" + pageSize).build());
    }

    @GetMapping("/cart/items")
    public Mono<Rendering> getCart() {
        return cartService.getUserCart(userLogin)
                .map(viewData -> Rendering.view("cart")
                        .modelAttribute("items", viewData.items())
                        .modelAttribute("total", viewData.total())
                        .build());
    }

    @PostMapping("/cart/items")
    public Mono<Rendering> editCartItemsFromCart(
            @RequestParam long id,
            @RequestParam ActionDto action) {
        return cartService.editCountItemCart(userLogin, id, action)
                .then(cartService.getUserCart(userLogin))
                .map(viewData -> Rendering.view("cart")
                        .modelAttribute("items", viewData.items())
                        .modelAttribute("total", viewData.total())
                        .build());
    }
}
