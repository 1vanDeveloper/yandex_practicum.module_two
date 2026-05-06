package ru.yandex.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.dto.CartActionForm;
import ru.yandex.practicum.dto.ItemsActionForm;
import ru.yandex.practicum.security.SecurityUser;
import ru.yandex.practicum.service.CartService;

@Slf4j
@Controller
class CartController {

    private final CartService cartService;

    CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping(value = "/items", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<Rendering> editCartItemsFromItems(
            @ModelAttribute ItemsActionForm form,
            @AuthenticationPrincipal SecurityUser currentUser) {

        log.info("POST /items - user={}, form={}", currentUser.getUsername(), form);

        return cartService.editCountItemCart(currentUser.getUsername(), form.id(), form.action())
                .doOnError(e -> log.error("Error editing cart for user={}, id={}, action={}: {}",
                        currentUser.getUsername(), form.id(), form.action(), e.getMessage()))
                .thenReturn(Rendering.redirectTo(
                        "/items?search=" + form.search() +
                        "&sort=" + form.sort() +
                        "&pageNumber=" + form.pageNumber() +
                        "&pageSize=" + form.pageSize()).build());
    }

    @PostMapping(value = "/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<Rendering> editCartItemsFromItem(
            @PathVariable long id,
            @ModelAttribute CartActionForm form,
            @AuthenticationPrincipal SecurityUser currentUser) {

        log.info("POST /items/{} - user={}, form={}", id, currentUser.getUsername(), form);

        return cartService.editCountItemCart(currentUser.getUsername(), id, form.action())
                .doOnError(e -> log.error("Error editing cart for user={}, id={}, action={}: {}",
                        currentUser.getUsername(), id, form.action(), e.getMessage()))
                .thenReturn(Rendering.redirectTo("/items/" + id).build());
    }

    @GetMapping("/cart/items")
    public Mono<Rendering> getCart(@AuthenticationPrincipal SecurityUser currentUser) {
        log.info("GET /cart/items - user={}", currentUser.getUsername());
        return cartService.getUserCart(currentUser.getUsername())
                .map(viewData -> Rendering.view("cart")
                        .modelAttribute("items", viewData.items())
                        .modelAttribute("total", viewData.total())
                        .build());
    }

    @PostMapping(value = "/cart/items", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<Rendering> editCartItemsFromCart(
            @ModelAttribute CartActionForm form,
            @AuthenticationPrincipal SecurityUser currentUser) {

        log.info("POST /cart/items - user={}, form={}", currentUser.getUsername(), form);

        return cartService.editCountItemCart(currentUser.getUsername(), form.id(), form.action())
                .doOnError(e -> log.error("Error editing cart for user={}, id={}, action={}: {}",
                        currentUser.getUsername(), form.id(), form.action(), e.getMessage()))
                .then(cartService.getUserCart(currentUser.getUsername()))
                .map(viewData -> Rendering.view("cart")
                        .modelAttribute("items", viewData.items())
                        .modelAttribute("total", viewData.total())
                        .build());
    }
}
