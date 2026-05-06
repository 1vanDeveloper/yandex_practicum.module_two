package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.yandex.practicum.WithMockSecurityUser;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithMockSecurityUser(username = "user")
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
    @WithMockSecurityUser(username = "user")
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
    @WithMockSecurityUser(username = "user")
    void editCartItemsFromItems() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("id", "1");
        formData.add("action", "PLUS");
        formData.add("search", "");
        formData.add("sort", "ALPHA");
        formData.add("pageSize", "10");
        formData.add("pageNumber", "1");

        webTestClient.post()
                .uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", value -> assertThat(value).startsWith("/items"));
    }

    @Test
    @WithMockSecurityUser(username = "user")
    void getItem() {
        webTestClient.get()
                .uri("/items/{id}", 1L)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockSecurityUser(username = "user")
    void getCart() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockSecurityUser()
    void editCartItemsFromCart() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("id", "1");
        formData.add("action", "PLUS");

        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockSecurityUser()
    void getOrders() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockSecurityUser()
    void testGetOrders() {
        webTestClient.get()
                .uri("/orders/{id}", 1L)
                .exchange()
                .expectStatus().isOk();
    }
}
