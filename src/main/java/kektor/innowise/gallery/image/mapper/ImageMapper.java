package kektor.innowise.gallery.image.mapper;

import kektor.innowise.gallery.image.dto.ImageDto;
import kektor.innowise.gallery.image.dto.UploadRequestDto;
import kektor.innowise.gallery.image.model.Image;
import kektor.innowise.gallery.image.model.Like;
import kektor.innowise.gallery.image.msg.LikeEventMessage;
import kektor.innowise.gallery.security.UserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapConfig.class)
public interface ImageMapper {

    Image toModel(UploadRequestDto registrationRequest, Long userId, String s3key);

    ImageDto toDto(Image image, String username, String url, boolean isLiked);

    @Mapping(source = "imageId", target = "image.id")
    Like toModel(Long imageId, Long userId);

    @Mapping(target = "imageId", source = "image.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "imageOwnerId", source = "image.userId")
    @Mapping(target = "instant", expression = "java(Instant.now())")
    LikeEventMessage toEvent(ImageDto image, UserPrincipal user, LikeEventMessage.EventType eventType);

}