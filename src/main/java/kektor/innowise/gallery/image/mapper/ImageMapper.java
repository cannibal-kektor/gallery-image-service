package kektor.innowise.gallery.image.mapper;

import kektor.innowise.gallery.image.dto.ImageDto;
import kektor.innowise.gallery.image.dto.UploadRequestDto;
import kektor.innowise.gallery.image.model.Image;
import kektor.innowise.gallery.image.model.Like;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapConfig.class)
public interface ImageMapper {

    Image toModel(UploadRequestDto registrationRequest, Long userId, String s3key);

    ImageDto toDto(Image image, String username, String url, boolean isLiked);

    @Mapping(source = "imageId", target = "image.id")
    Like toModel(Long imageId, Long userId);

}