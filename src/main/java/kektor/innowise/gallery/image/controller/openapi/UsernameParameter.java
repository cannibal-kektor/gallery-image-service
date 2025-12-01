package kektor.innowise.gallery.image.controller.openapi;


import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

@Parameter(
        name = "username",
        description = "Username to get images for",
        required = true,
        example = "alex_white",
        schema = @Schema(type = "string", minLength = 3, maxLength = 30)
)
public @interface UsernameParameter {
}
