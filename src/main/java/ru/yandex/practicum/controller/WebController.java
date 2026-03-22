package ru.yandex.practicum.controller;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.model.CartItem;
import ru.yandex.practicum.model.Item;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.model.OrderItem;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.ItemService;
import ru.yandex.practicum.service.OrderService;
import ru.yandex.practicum.service.model.SortOrder;

import java.util.List;
import java.util.NoSuchElementException;
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

        var sortOrder = switch (sort) {
            case NO -> SortOrder.NO;
            case ALPHA -> SortOrder.ALPHA;
            case PRICE -> SortOrder.PRICE;
        };
        return itemService.getItems(search, sortOrder, pageSize, pageNumber).thenApplyAsync(page -> {
            var viewData = new GetItemsViewDto(
                    search,
                    sort,
                    page.stream().map(WebController::convert).toList(),
                    new PagingDto(
                            page.getNumber(),
                            page.getSize(),
                            page.hasPrevious(),
                            page.hasNext()));

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

        return itemService.getItem(id).thenApplyAsync(item -> {
            if (item.isEmpty()) {
                throw new NoSuchElementException("id: " + id);
            }
            var viewData = convert(item.get());

            model.addAttribute("item", viewData);
            return "item";
        });
    }

    @Async
    @GetMapping("/cart/items")
    public CompletableFuture<String> getCart(Model model) {
        return cartService.getUserCart(userLogin).thenApplyAsync(cart -> {
            var viewData = new GetCartViewDto(
                    cart.getCartItems()
                            .stream()
                            .map(WebController::convert)
                            .toList());

            model.addAttribute("items", viewData.items());
            model.addAttribute("total", viewData.total());
            return "cart";
        });
    }

    @Async
    @GetMapping("/orders")
    public CompletableFuture<String> getOrders(Model model) {
        return orderService.getUserOrders(userLogin).thenApplyAsync(orders -> {
            var viewData = new GetOrdersViewDto(
                    orders.stream().map(WebController::convert).toList()
            );

            model.addAttribute("orders", viewData.orders());
            return "orders";
        });
    }

    @Async
    @GetMapping("/orders/{id}")
    public CompletableFuture<String>  getOrders(Model model,
                                                @PathVariable long id,
                                                @RequestParam(required = false) Boolean newOrder) {
        return orderService.getOrder(id).thenApplyAsync(order -> {
            var viewData = convert(order);
            model.addAttribute("order", viewData);
            return "order";
        });
    }

    private static ItemDto convert(Item item) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                "/images/" + item.getId(),
                item.getPrice().doubleValue(),
                item.getCount()
        );
    }

    private static ItemDto convert(CartItem cartItem) {
        return new ItemDto(
                cartItem.getId(),
                cartItem.getItem().getTitle(),
                cartItem.getItem().getDescription(),
                "/images/" + cartItem.getItem().getId(),
                cartItem.getItem().getPrice().doubleValue(),
                cartItem.getCount()
        );
    }

    private static OrderDto convert(Order order) {
        return new OrderDto(order.getId(),
                order.getOrderItems()
                        .stream()
                        .map(WebController::convert)
                        .toList());
    }

    private static OrderItemDto convert(OrderItem orderItem) {
        return new OrderItemDto(orderItem.getTitle(), orderItem.getCount(), orderItem.getPrice().doubleValue());
    }
}
