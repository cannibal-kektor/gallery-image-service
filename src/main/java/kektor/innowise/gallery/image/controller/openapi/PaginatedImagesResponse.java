package kektor.innowise.gallery.image.controller.openapi;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kektor.innowise.gallery.image.dto.ImageDto;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "200",
        description = "Images retrieved successfully",
        content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schemaProperties = {
                        @SchemaProperty(
                                name = "content",
                                array = @ArraySchema(
                                        schema = @Schema(implementation = ImageDto.class))
                        ),
                        @SchemaProperty(
                                name = "hasNext",
                                schema = @Schema(
                                        type = "boolean",
                                        description = "Whether more results are available"
                                )
                        )
                }
        )
)
public @interface PaginatedImagesResponse {
}