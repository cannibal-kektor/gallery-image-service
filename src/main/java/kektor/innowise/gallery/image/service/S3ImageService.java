package kektor.innowise.gallery.image.service;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import kektor.innowise.gallery.image.exception.ImageUploadException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import static kektor.innowise.gallery.image.conf.CacheConfig.URLS_CACHE_LOCAL;
import static kektor.innowise.gallery.image.conf.CacheConfig.URLS_CACHE_REMOTE;
import static kektor.innowise.gallery.image.helper.ImageUtils.extractExtension;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class S3ImageService {

    public static String S3_KEY_TEMPLATE = "users/%d/%s%s";
    static String IMAGES_BUCKET_NAME = "images";

    S3Template s3Template;

    public void uploadToS3(MultipartFile file, String s3Key) {
        ObjectMetadata metadata = ObjectMetadata.builder()
                .contentLength(file.getSize())
                .contentType(file.getContentType())
                .cacheControl("public, max-age=86400")
                .build();
        try {
            s3Template.upload(IMAGES_BUCKET_NAME, s3Key,
                    file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new ImageUploadException(file.getOriginalFilename());
        }
    }

    @Cacheable(cacheNames = {URLS_CACHE_LOCAL, URLS_CACHE_REMOTE})
    public String getSignedUrlForImage(String s3key) {
        return s3Template.createSignedGetURL(IMAGES_BUCKET_NAME, s3key, Duration.ofHours(2))
                .toString();
    }

    public String generateS3Key(MultipartFile file, Long userId) {
        String extension = extractExtension(file.getOriginalFilename());
        String key = UUID.randomUUID().toString();
        return S3_KEY_TEMPLATE.formatted(userId, key, extension);
    }

}