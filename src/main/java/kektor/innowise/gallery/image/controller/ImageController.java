package kektor.innowise.gallery.image.controller;

import jakarta.validation.Valid;
import kektor.innowise.gallery.image.controller.openapi.ImageServiceOpenApi;
import kektor.innowise.gallery.image.dto.ImageDto;
import kektor.innowise.gallery.image.dto.KeySetScrollRequest;
import kektor.innowise.gallery.image.dto.UpdateRequestDto;
import kektor.innowise.gallery.image.dto.UploadRequestDto;
import kektor.innowise.gallery.image.service.ImageService;
import kektor.innowise.gallery.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Window;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController implements ImageServiceOpenApi {

    final ImageService imageService;

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<ImageDto> get(@PathVariable Long id) {
        return ResponseEntity.ok()
                .body(imageService.getById(id));
    }

    @PostMapping(
            path = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces =  MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<ImageDto> upload(@Valid @ModelAttribute UploadRequestDto uploadRequest) {
        return ResponseEntity.ok()
                .body(imageService.save(uploadRequest));
    }

    @PutMapping(
            path = "/{imageId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<ImageDto> update(@PathVariable Long imageId,
                                           @Valid @RequestBody UpdateRequestDto updateRequest) {
        return ResponseEntity.ok()
                .body(imageService.updatePost(imageId, updateRequest));
    }

    @DeleteMapping("/{imageId}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable Long imageId) {
        imageService.deletePost(imageId);
        return ResponseEntity.ok().build();
    }


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<Window<ImageDto>> getAll(KeySetScrollRequest scrollRequest) {
        return ResponseEntity.ok()
                .body(imageService.getAll(scrollRequest));
    }

    @GetMapping(
            path = "/username/{username}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<Window<ImageDto>> getUserImages(@PathVariable String username,
                                                          KeySetScrollRequest scrollRequest) {
        return ResponseEntity.ok()
                .body(imageService.getUserImages(username, scrollRequest));
    }

    @GetMapping(
            path = "/user/current",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Override
    public ResponseEntity<Window<ImageDto>> getCurrentUserImages(KeySetScrollRequest scrollRequest,
                                                                 @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok()
                .body(imageService.getUserImages(user.id(), scrollRequest));
    }

    @PostMapping("/{imageId}/like")
    @Override
    public ResponseEntity<ImageDto> likeImage(@PathVariable Long imageId) {
        return ResponseEntity.ok(imageService.likeImage(imageId));
    }

    @GetMapping(
            path = "/{imageId}/internal",
            headers = "X-System-Internal-Call",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ImageDto> getInternal(@PathVariable Long imageId) {
        return ResponseEntity.ok()
                .body(imageService.getByIdInternal(imageId));
    }

}