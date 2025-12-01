package kektor.innowise.gallery.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kektor.innowise.gallery.image.validation.ValidImage;
import org.springframework.web.multipart.MultipartFile;

@Schema(
        name = "UploadRequestDto",
        description = "Request model for uploading a new image to the gallery"
)
public record UploadRequestDto(

        @Schema(
                description = "Image description text",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Test image description"
        )
        @NotBlank
        @Size(max = 1000)
        String description,

        @Schema(
                description = "Image file to upload (JPEG, PNG, max 50MB)",
                requiredMode = Schema.RequiredMode.REQUIRED,
                format = "binary"
        )
        @ValidImage
        MultipartFile imageFile
) {
}
