package ru.yandex.practicum.controller;

import jakarta.validation.constraints.Min;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.service.ImageService;

@RestController
@RequestMapping("/images")
@Validated
class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Загрузка картинки товара в БД
     * @param itemId идентификатор товара
     * @param filePart файл с картинкой для записи
     * @return строка с ошибкой, если произошла
     */
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> uploadImage(
            @PathVariable("id") @Min(1) long itemId,
            @RequestPart("image") FilePart filePart) {

        String originalFilename = filePart.filename();

        if (originalFilename.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body("empty file name"));
        }

        // Проверка расширения
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == originalFilename.length() - 1) {
            return Mono.just(ResponseEntity.badRequest().body("file name has no extension"));
        }

        return imageService.saveImage(filePart, itemId)
                .thenReturn(ResponseEntity.ok("ok"))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body("upload failed: " + e.getMessage())));
    }

    /**
     * Получение картинки товара
     * @param itemId идентификатор товара
     * @return тело картинки
     */
    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<Resource>> getImage(@PathVariable("id") @Min(1) long itemId) {
        return imageService.getImage(itemId)
                .map(resource -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getRight() + "\"")
                        .body(resource.getLeft()))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }
}
