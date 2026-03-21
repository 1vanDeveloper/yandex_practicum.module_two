package ru.yandex.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.*;

import java.util.List;

@Controller
class WebController {

    @GetMapping({"/", "/items"})
    public String getItems(
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALPHA") SortDto sort,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber) {

        var viewData = new GetItemsViewDto(
                search,
                sort,
                List.of(emptyItem()),
                new PagingDto(
                        pageNumber,
                        pageSize,
                        false,
                        true));

        model.addAttribute("search", viewData.search());
        model.addAttribute("sort", viewData.sort().name());
        model.addAttribute("items", viewData.items());
        model.addAttribute("paging", viewData.paging());
        return "items";
    }

    @GetMapping("/items/{id}")
    public String getItem(
            Model model,
            @PathVariable long id) {

        var viewData = emptyItem();

        model.addAttribute("item", viewData);
        return "item";
    }

    @GetMapping("/cart/items")
    public String getCart(Model model) {
        var viewData = new GetCartViewDto(List.of(emptyItem()));

        model.addAttribute("items", viewData.items());
        model.addAttribute("total", viewData.total());
        return "cart";
    }

    @GetMapping("/orders")
    public String getOrders(Model model) {
        var viewData = new GetOrdersViewDto(
                List.of(
                        new OrderDto(1, List.of(new OrderItemDto("title", 5, 50)))
                )
        );

        model.addAttribute("orders", viewData.orders());
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrders(Model model, @PathVariable long id, @RequestParam(required = false) Boolean newOrder) {

        var title = newOrder != null && newOrder ? "new order" : "title";
        var viewData = new OrderDto(id, List.of(new OrderItemDto(title, 5, 50)));

        model.addAttribute("order", viewData);
        return "order";
    }

    private static ItemDto emptyItem() {
        return new ItemDto(1, "title", "description", "/img/1.png", 100, 10);
    }
}
