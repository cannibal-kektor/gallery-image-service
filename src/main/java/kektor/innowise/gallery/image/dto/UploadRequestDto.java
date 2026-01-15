package kektor.innowise.gallery.image.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kektor.innowise.gallery.image.validation.ValidImage;
import org.springframework.web.multipart.MultipartFile;

public record UploadRequestDto(
        @NotBlank
        @Size(max = 1000)
        String description,
        @ValidImage
        MultipartFile imageFile
) {
}
