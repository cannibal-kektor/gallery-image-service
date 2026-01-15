package kektor.innowise.gallery.image.controller.openapi;


import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

@Parameter(
        name = "imageId",
        description = "Unique Image id",
        example = "12345",
        in = ParameterIn.PATH,
        schema = @Schema(type = "integer", format = "int64", minimum = "1")
)
public @interface ImageIdParameter {
}
