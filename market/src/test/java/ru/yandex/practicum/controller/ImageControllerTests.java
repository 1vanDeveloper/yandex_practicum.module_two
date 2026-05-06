package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest
@AutoConfigureWebTestClient
public class ImageControllerTests {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithMockUser(username = "user1")
    void testUploadImage_emptyFile_badRequest() {
        var pngStub = new byte[0];
        var fileName = "empty.png";

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new ByteArrayResource(pngStub))
                .filename(fileName)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        webTestClient.put()
                .uri("/images/{id}", 1L)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user1")
    void testGetImage_imageNotFound_400() {
        webTestClient.get()
                .uri("/images/{id}", 10000000L)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(username = "user1")
    void testUploadAndGetImage_success() {
        var pngStub = new byte[]{(byte) 137, 80, 78, 71};
        var fileName = "post_image.png";

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new ByteArrayResource(pngStub))
                .filename(fileName)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        webTestClient.put()
                .uri("/images/{id}", 1L)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("ok");

        webTestClient.get()
                .uri("/images/{id}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectBody(byte[].class)
                .isEqualTo(pngStub);
    }
}
