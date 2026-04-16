package ru.yandex.practicum.service;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.Image;
import ru.yandex.practicum.repository.ImageRepository;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Сервис по управлению картинками
 */
public interface ImageService {
    Mono<Image> saveImage(FilePart image, long itemId);

    Mono<Pair<Resource, String>> getImage(long itemId);
}

@Service
class ImplementedImageService implements ImageService {

    private final ImageRepository imageRepository;

    public ImplementedImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Mono<Image> saveImage(FilePart image, long itemId) {
        return imageRepository.findByItemId(itemId)
                .flatMap(existsImage -> updateImage(existsImage, image))
                .switchIfEmpty(createImage(image, itemId));
    }

    @Override
    public Mono<Pair<Resource, String>> getImage(long itemId) {
        return imageRepository.findByItemId(itemId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("itemId")))
                .map(image -> Pair.of(
                        new ByteArrayResource(image.getContent()),
                        image.getFileName()));
    }

    private Mono<Image> updateImage(Image existsImage, FilePart image) {
        return getBytesFromFilePart(image).flatMap(bytes -> {
                    existsImage.setFileName(image.filename());
                    existsImage.setContent(bytes);
                    return imageRepository.save(existsImage);
                })
                .onErrorMap(RuntimeException::new);
    }

    private Mono<Image> createImage(FilePart image, long itemId) {
        return getBytesFromFilePart(image).flatMap(bytes -> {
                    var imageEntity = new Image();
                    imageEntity.setItemId(itemId);
                    imageEntity.setFileName(image.filename());
                    imageEntity.setContent(bytes);
                    return imageRepository.save(imageEntity);
                })
                .onErrorMap(RuntimeException::new);
    }

    public Mono<byte[]> getBytesFromFilePart(FilePart filePart) {
        return filePart.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    if (bytes.length == 0)
                    {
                        throw new RuntimeException("File is empty");
                    }
                    return bytes;
                })
                .collectList() // Собираем все части (если файл большой)
                .map(this::concatenateByteArrays);
    }

    private byte[] concatenateByteArrays(List<byte[]> list) {
        int totalLength = list.stream().mapToInt(b -> b.length).sum();
        byte[] result = new byte[totalLength];
        int currentPosition = 0;
        for (byte[] array : list) {
            System.arraycopy(array, 0, result, currentPosition, array.length);
            currentPosition += array.length;
        }

        return result;
    }
}
