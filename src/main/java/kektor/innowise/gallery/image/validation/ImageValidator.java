package kektor.innowise.gallery.image.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

import static kektor.innowise.gallery.image.helper.ImageUtils.extractExtension;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    Set<String> allowedExtensions = Set.of(".jpg", ".jpeg", ".png");
    Set<String> allowedMimeTypes = Set.of("image/jpeg", "image/png");

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        String extension = extractExtension(file.getOriginalFilename());
        return allowedExtensions.contains(extension) &&
                allowedMimeTypes.contains(file.getContentType());
    }

}
