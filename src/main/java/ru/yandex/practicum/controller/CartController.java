package ru.yandex.practicum.controller;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.dto.ActionDto;
import ru.yandex.practicum.dto.SortDto;
import ru.yandex.practicum.service.CartService;

import java.util.concurrent.CompletableFuture;

@Controller
class CartController {

    private final String userLogin = "user";
    private final CartService cartService;

    CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Async
    @PostMapping("/items")
    public CompletableFuture<String> editCartItemsFromItems(
            @RequestParam long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALPHA") SortDto sort,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam ActionDto action) {

        return cartService.editCountItemCart(userLogin, id, action).thenApplyAsync(v -> UriComponentsBuilder.fromPath("redirect:/items")
                .queryParam("search", search)
                .queryParam("sort", sort)
                .queryParam("pageNumber", pageNumber)
                .queryParam("pageSize", pageSize)
                .toUriString());
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
    @PostMapping("/cart/items")
    public CompletableFuture<String> editCartItemsFromCart(
            Model model,
            @RequestParam long id,
            @RequestParam ActionDto action) {
        return cartService.editCountItemCart(userLogin, id, action)
                .thenComposeAsync(v -> cartService.getUserCart(userLogin))
                .thenApplyAsync(viewData -> {
                    model.addAttribute("items", viewData.items());
                    model.addAttribute("total", viewData.total());
                    return "cart";
                });
    }
}
