package kektor.innowise.gallery.image.controller.openapi;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.FailedApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kektor.innowise.gallery.image.dto.ImageDto;
import kektor.innowise.gallery.image.dto.KeySetScrollRequest;
import kektor.innowise.gallery.image.dto.UpdateRequestDto;
import kektor.innowise.gallery.image.dto.UploadRequestDto;
import kektor.innowise.gallery.security.UserPrincipal;
import org.springframework.data.domain.Window;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

import static kektor.innowise.gallery.image.conf.OpenApiConfig.CURSOR_ID;
import static kektor.innowise.gallery.image.conf.OpenApiConfig.CURSOR_LIKES_COUNT;
import static kektor.innowise.gallery.image.conf.OpenApiConfig.CURSOR_UPLOADED_AT;
import static kektor.innowise.gallery.image.conf.OpenApiConfig.INTERNAL_SERVICE_AUTH;
import static kektor.innowise.gallery.image.conf.OpenApiConfig.JWT_BEARER_TOKEN;
import static kektor.innowise.gallery.image.conf.OpenApiConfig.PROBLEM_DETAIL_RESPONSE;
import static kektor.innowise.gallery.image.conf.OpenApiConfig.SORT_CRITERIA;
import static kektor.innowise.gallery.image.conf.OpenApiConfig.TILL_DATE;
import static kektor.innowise.gallery.image.conf.OpenApiConfig.WINDOW_SIZE;

@Tag(
        name = "Image Management API",
        description = "API for managing images in the Image Gallery system."
)
@FailedApiResponse(ref = PROBLEM_DETAIL_RESPONSE)
public interface ImageServiceOpenApi {

    @Operation(
            summary = "Get image by ID",
            description = "Retrieves a specific image by its id.",
            security = {
                    @SecurityRequirement(name = JWT_BEARER_TOKEN),
                    @SecurityRequirement(name = INTERNAL_SERVICE_AUTH)
            }
    )
    @ImageDtoResponse
    ResponseEntity<ImageDto> get(@ImageIdParameter Long imageId);

    @Operation(
            summary = "Upload new image",
            description = """
                    Uploads a new image to the gallery with description.
                    Supported formats: JPEG, PNG. Maximum file size: 50MB.
                    """,
            requestBody = @RequestBody(
                    description = "Image upload request with file and description",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = UploadRequestDto.class)
                    )
            ),
            security = @SecurityRequirement(name = JWT_BEARER_TOKEN)

    )
    @ImageDtoResponse
    ResponseEntity<ImageDto> upload(@Valid @ModelAttribute UploadRequestDto uploadRequest);

    @Operation(
            summary = "Update image description",
            description = "Updates the description of an existing image.",
            requestBody = @RequestBody(
                    description = "Updated image description",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateRequestDto.class)
                    )
            ),
            security = @SecurityRequirement(name = JWT_BEARER_TOKEN)
    )
    @ImageDtoResponse
    ResponseEntity<ImageDto> update(@ImageIdParameter Long imageId,
                                    @Valid UpdateRequestDto updateRequest);

    @Operation(
            summary = "Delete image",
            description = "Deletes a specific image and its associated comments",
            security = @SecurityRequirement(name = JWT_BEARER_TOKEN)
    )
    @ApiResponse(
            responseCode = "200",
            description = "Image deleted successfully"
    )
    ResponseEntity<Void> delete(@ImageIdParameter Long imageId);

    @Operation(
            summary = "Get all images with pagination",
            description = "Retrieves paginated images using keyset pagination for scrolling",
            parameters = {
                    @Parameter(ref = WINDOW_SIZE),
                    @Parameter(ref = SORT_CRITERIA),
                    @Parameter(ref = TILL_DATE),
                    @Parameter(ref = CURSOR_UPLOADED_AT),
                    @Parameter(ref = CURSOR_LIKES_COUNT),
                    @Parameter(ref = CURSOR_ID)
            },
            security = @SecurityRequirement(name = JWT_BEARER_TOKEN)

    )
    @PaginatedImagesResponse
    ResponseEntity<Window<ImageDto>> getAll(@Parameter(hidden = true) KeySetScrollRequest scrollRequest);


    @Operation(
            summary = "Get user images by username",
            description = "Retrieves paginated images for a specific user by username using keyset pagination",
            parameters = {
                    @Parameter(ref = WINDOW_SIZE),
                    @Parameter(ref = SORT_CRITERIA),
                    @Parameter(ref = TILL_DATE),
                    @Parameter(ref = CURSOR_UPLOADED_AT),
                    @Parameter(ref = CURSOR_LIKES_COUNT),
                    @Parameter(ref = CURSOR_ID)
            },
            security = @SecurityRequirement(name = JWT_BEARER_TOKEN)

    )
    @PaginatedImagesResponse
    ResponseEntity<Window<ImageDto>> getUserImages(@UsernameParameter String username,
                                                   @Parameter(hidden = true) KeySetScrollRequest scrollRequest);

    @Operation(
            summary = "Get current user images",
            description = "Retrieves paginated images for the currently authenticated user using keyset pagination.",
            parameters = {
                    @Parameter(ref = WINDOW_SIZE),
                    @Parameter(ref = SORT_CRITERIA),
                    @Parameter(ref = TILL_DATE),
                    @Parameter(ref = CURSOR_UPLOADED_AT),
                    @Parameter(ref = CURSOR_LIKES_COUNT),
                    @Parameter(ref = CURSOR_ID)
            },
            security = @SecurityRequirement(name = JWT_BEARER_TOKEN)
    )
    @PaginatedImagesResponse
    ResponseEntity<Window<ImageDto>> getCurrentUserImages(@Parameter(hidden = true) KeySetScrollRequest scrollRequest,
                                                          UserPrincipal user);

    @Operation(
            summary = "Like or unlike image",
            description = """
                    Toggles like status for a specific image.
                    If the image is already liked, it will be unliked.
                    """,
            security = @SecurityRequirement(name = JWT_BEARER_TOKEN)
    )
    @ImageDtoResponse
    ResponseEntity<ImageDto> likeImage(@ImageIdParameter Long imageId);

}
