package kektor.innowise.gallery.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "UpdateRequestDto",
        description = "Request model for updating image description"
)
public record UpdateRequestDto(

        @Schema(
                description = "Image description text",
                example = "Test image description",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        @NotBlank
        @Size(max = 1000)
        String description

) {
}