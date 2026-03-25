package ru.yandex.practicum.service;

import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.model.Image;
import ru.yandex.practicum.repository.ImageRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
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
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public ImplementedImageService(ImageRepository imageRepository, EntityManager entityManager, TransactionTemplate transactionTemplate) {
        this.imageRepository = imageRepository;
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public CompletableFuture<Image> saveImage(MultipartFile image, long itemId) {
        return CompletableFuture.supplyAsync(() -> transactionTemplate.execute(status -> {
            var optImage = imageRepository.getImageByItemId(itemId);
            if (optImage.isEmpty()) {
                var imageEntity = new Image();
                imageEntity.setItemId(itemId);
                imageEntity.setFileName(image.getOriginalFilename());
                try {
                    imageEntity.setContent(image.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                entityManager.persist(imageEntity);
                return imageEntity;
            }

            var existsImage = optImage.get();
            existsImage.setItemId(itemId);
            existsImage.setFileName(image.getOriginalFilename());
            try {
                existsImage.setContent(image.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            entityManager.merge(existsImage);
            return existsImage;
        }));
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
