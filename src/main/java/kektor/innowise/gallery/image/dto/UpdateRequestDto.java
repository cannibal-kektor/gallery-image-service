package kektor.innowise.gallery.image.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRequestDto(
        @NotBlank
        @Size(max = 1000)
        String description
) {
}
