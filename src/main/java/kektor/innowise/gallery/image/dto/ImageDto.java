package kektor.innowise.gallery.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(
        name = "ImageDto",
        description = "Image data model containing image information"
)
public record ImageDto(

        @Schema(
                description = "Unique image ID",
                example = "12345",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long id,

        @Schema(
                description = "User ID who uploaded the image",
                example = "67890",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long userId,

        @Schema(
                description = "Username of the image uploader",
                example = "alex_white",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String username,

        @Schema(
                description = "Signed S3 URL for image access (valid for 2 hours)",
                example = "https://images.s3.amazonaws.com/users/67890/abc123.jpg?X-Amz-Signature=...",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String url,

        @Schema(
                description = "Image description text",
                example = "Test image description",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String description,

        @Schema(
                description = "Image upload timestamp",
                example = "2025-11-01T10:30:00Z",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Instant uploadedAt,

        @Schema(
                description = "Number of likes the image",
                example = "42",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Integer likesCount,

        @Schema(
                description = "Whether the current user has liked this image",
                example = "true",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        boolean isLiked
) {
}