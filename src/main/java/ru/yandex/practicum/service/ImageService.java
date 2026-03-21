package ru.yandex.practicum.service;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.model.Image;
import ru.yandex.practicum.repository.ImageRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис по управлению картинками
 */
public interface ImageService {
    @Async
    CompletableFuture<Image> saveImage(MultipartFile image, long itemId);

    @Async
    CompletableFuture<Pair<Resource, String>> getImage(long itemId);
}

@Service
class ImplementedImageService implements ImageService {

    private final ImageRepository imageRepository;

    public ImplementedImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public CompletableFuture<Image> saveImage(MultipartFile image, long itemId) {
        return CompletableFuture.supplyAsync(() -> {
            var imageEntity = new Image();
            imageEntity.setItemId(itemId);
            imageEntity.setFileName(image.getOriginalFilename());
            try {
                imageEntity.setContent(image.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return imageRepository.save(imageEntity);
        });
    }

    @Override
    public CompletableFuture<Pair<Resource, String>> getImage(long itemId) {
        return CompletableFuture.supplyAsync(() -> {
            var image = imageRepository.getImageByItemId(itemId);
            if (image.isEmpty()) {
                throw new NoSuchElementException("itemId");
            }

            return Pair.of(
                    new InputStreamResource(new ByteArrayInputStream(image.get().getContent())),
                    image.get().getFileName());
        });
    }
}
