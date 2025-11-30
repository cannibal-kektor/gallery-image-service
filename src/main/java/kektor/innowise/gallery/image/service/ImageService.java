package kektor.innowise.gallery.image.service;

import kektor.innowise.gallery.image.dto.ImageDeleted;
import kektor.innowise.gallery.image.dto.ImageDto;
import kektor.innowise.gallery.image.dto.KeySetScrollRequest;
import kektor.innowise.gallery.image.dto.UpdateRequestDto;
import kektor.innowise.gallery.image.dto.UploadRequestDto;
import kektor.innowise.gallery.image.dto.UserDto;
import kektor.innowise.gallery.image.exception.UserNotFoundException;
import kektor.innowise.gallery.image.exception.UsernameNotFoundException;
import kektor.innowise.gallery.image.mapper.ImageMapper;
import kektor.innowise.gallery.image.model.Image;
import kektor.innowise.gallery.image.model.Like;
import kektor.innowise.gallery.image.repository.ImageRepository;
import kektor.innowise.gallery.image.repository.LikeRepository;
import kektor.innowise.gallery.security.UserPrincipal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Window;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageService {

    ImageRepository imageRepository;
    LikeRepository likeRepository;
    S3ImageService s3Service;
    CommentServiceClient commentService;
    UserServiceClient userService;
    ApplicationEventPublisher eventPublisher;
    ImageMapper mapper;

    @Transactional
    public ImageDto save(UploadRequestDto uploadRequest) {
        Long userId = currentUserId();

        String s3Key = s3Service.generateS3Key(uploadRequest.imageFile(), userId);

        Image image = mapper.toModel(uploadRequest, userId, s3Key);
        image = imageRepository.save(image);

        s3Service.uploadToS3(uploadRequest.imageFile(), s3Key);

        return enrichData(image, _ -> false);
    }

    @Transactional(readOnly = true)
    public ImageDto getById(Long imageId) {
        Image image = imageRepository.findByIdExceptionally(imageId);
        return enrichData(image, id ->
                likeRepository.existsByImageIdAndUserId(id, currentUserId()));
    }

    @Transactional(readOnly = true)
    public Window<ImageDto> getAll(KeySetScrollRequest scrollRequest) {
        Window<Image> result = scrollRequest.tillDate() == null ?
                imageRepository.findAllFilteredBy(
                        scrollRequest.scrollPosition(),
                        scrollRequest.sort(),
                        scrollRequest.limit()) :
                imageRepository.findAllFilteredByUploadedAtAfter(
                        scrollRequest.tillDate(),
                        scrollRequest.scrollPosition(),
                        scrollRequest.sort(),
                        scrollRequest.limit());
        return enrichData(result);
    }

    @Transactional(readOnly = true)
    public Window<ImageDto> getUserImages(Long userId, KeySetScrollRequest scrollRequest) {
        Window<Image> result = scrollRequest.tillDate() == null ?
                imageRepository.findAllFilteredByUserId(
                        userId,
                        scrollRequest.scrollPosition(),
                        scrollRequest.sort(),
                        scrollRequest.limit()) :
                imageRepository.findAllFilteredByUserIdAndUploadedAtAfter(
                        userId,
                        scrollRequest.tillDate(),
                        scrollRequest.scrollPosition(),
                        scrollRequest.sort(),
                        scrollRequest.limit());
        return enrichData(result);
    }

    @Transactional(readOnly = true)
    public Window<ImageDto> getUserImages(String username, KeySetScrollRequest scrollRequest) {
        Long userId = userService.fetchUser(username)
                .map(UserDto::id)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return getUserImages(userId, scrollRequest);
    }

    @Transactional
    public ImageDto likeImage(Long imageId) {
        Long userId = currentUserId();
        likeRepository.findIdByImageIdAndUserId(imageId, userId)
                .ifPresentOrElse(
                        like -> {
                            if (likeRepository.deleteLikeById(like.getId()) > 0) {
                                imageRepository.decrementLikes(imageId);
                            }
                        },
                        () -> {
                            Like like = mapper.toModel(imageId, userId);
                            likeRepository.save(like);
                            imageRepository.incrementLikes(imageId);
                        }
                );
        return getById(imageId);
    }

    @Transactional
    public ImageDto updatePost(Long imageId, UpdateRequestDto updateRequest) {
        Image image = imageRepository.findByIdAuthorized(imageId, currentUserId());
        image.setDescription(updateRequest.description());
        return enrichData(image, id ->
                likeRepository.existsByImageIdAndUserId(id, currentUserId()));
    }

    @Transactional
    public void deletePost(Long imageId) {
        imageRepository.findByIdAuthorized(imageId, currentUserId());
        imageRepository.deleteById(imageId);
        eventPublisher.publishEvent(new ImageDeleted(imageId));
    }

    @TransactionalEventListener(classes = ImageDeleted.class, phase = TransactionPhase.AFTER_COMMIT)
    public void clearOrphanComments(ImageDeleted event) {
        commentService.deleteImageComments(event.imageId());
    }

    private Window<ImageDto> enrichData(Window<Image> images) {
        List<Long> imageIds = images.stream()
                .map(Image::getId)
                .toList();
        Set<Long> likedImageIds = likeRepository.findLikedImagesIdByUserFromSpecific(currentUserId(), imageIds);
        return images.map(image -> enrichData(image, likedImageIds::contains));
    }

    private ImageDto enrichData(Image image, Predicate<Long> isLiked) {
        String url = s3Service.getSignedUrlForImage(image.getS3key());
        String username = userService.fetchUser(image.getUserId())
                .map(UserDto::username)
                .orElseThrow(() -> new UserNotFoundException(image.getUserId()));
        return mapper.toDto(image, username, url, isLiked.test(image.getId()));
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if (principal instanceof UserPrincipal user) {
            return user.id();
        } else
            return 0L;
    }
}