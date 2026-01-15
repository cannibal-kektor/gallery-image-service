package kektor.innowise.gallery.image.dto;

import java.time.Instant;

public record ImageDto(
        Long id,
        Long userId,
        String username,
        String url,
        String description,
        Instant uploadedAt,
        Integer likesCount,
        boolean isLiked
) {
}