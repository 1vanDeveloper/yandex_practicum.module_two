package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getItems_request_without_params() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertThat(body).contains("Витрина магазина");
                });
    }

    @Test
    void getItems_request_with_params() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/")
                        .queryParam("search", "phone")
                        .queryParam("sort", "PRICE")
                        .queryParam("pageSize", "2")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    void editCartItemsFromItems() {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .queryParam("pageNumber", "1")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", value -> assertThat(value).startsWith("/items"));
    }

    @Test
    void getItem() {
        webTestClient.get()
                .uri("/items/{id}", 1L)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getCart() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void editCartItemsFromCart() {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrders() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testGetOrders() {
        webTestClient.get()
                .uri("/orders/{id}", 1L)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void buy() {
        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", value -> assertThat(value).contains("newOrder"));
    }
}
