package kektor.innowise.gallery.image.service;

import kektor.innowise.gallery.image.dto.ImageDeleted;
import kektor.innowise.gallery.image.dto.ImageDto;
import kektor.innowise.gallery.image.dto.KeySetScrollRequest;
import kektor.innowise.gallery.image.dto.UpdateRequestDto;
import kektor.innowise.gallery.image.dto.UploadRequestDto;
import kektor.innowise.gallery.image.dto.UserDto;
import kektor.innowise.gallery.image.exception.ImageNotFoundException;
import kektor.innowise.gallery.image.exception.NonAuthorizedImageAccessException;
import kektor.innowise.gallery.image.exception.UserNotFoundException;
import kektor.innowise.gallery.image.mapper.ImageMapper;
import kektor.innowise.gallery.image.model.Image;
import kektor.innowise.gallery.image.model.Like;
import kektor.innowise.gallery.image.repository.ImageRepository;
import kektor.innowise.gallery.image.repository.LikeRepository;
import kektor.innowise.gallery.security.HeaderAuthenticationToken;
import kektor.innowise.gallery.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @Mock
    ImageRepository imageRepository;
    @Mock
    LikeRepository likeRepository;
    @Mock
    S3ImageService s3Service;
    @Mock
    UserServiceClient userServiceClient;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    ImageMapper mapper;

    @InjectMocks
    ImageService imageService;

    final Long userId = 1L;
    Image image;
    UploadRequestDto uploadRequestDto;
    MultipartFile multipartFile;

    @BeforeEach
    void setUpTestData() {
        image = createTestImage();
        multipartFile = new MockMultipartFile("file", "TestData".getBytes());
        uploadRequestDto = new UploadRequestDto("Test description", multipartFile);
    }

    @BeforeEach
    void setUpSecurity() {
        UserPrincipal userPrincipal = new UserPrincipal(userId, "testuser", "test@email.com");
        HeaderAuthenticationToken authenticationToken = new HeaderAuthenticationToken(userPrincipal);
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(securityContext);
    }

    Image createTestImage() {
        Image img = new Image();
        img.setId(1L);
        img.setUserId(1L);
        img.setS3key("users/1/test-s3-key");
        img.setDescription("Test description");
        img.setUploadedAt(Instant.now());
        img.setLikesCount(5);
        return img;
    }


    @Test
    void saveImage_When_ValidUploadRequest() {
        when(s3Service.generateS3Key(multipartFile, 1L))
                .thenReturn("users/1/test-s3-key");
        when(mapper.toModel(uploadRequestDto, 1L, "users/1/test-s3-key"))
                .thenReturn(image);
        when(imageRepository.save(image))
                .thenReturn(image);
        when(s3Service.getSignedUrlForImage("users/1/test-s3-key"))
                .thenReturn("https://s3.url/image.jpg");
        when(userServiceClient.fetchUser(image.getUserId()))
                .thenReturn(Optional.of(new UserDto(1L, "testUsername", "testEmail")));
        ImageDto expectedDto = new ImageDto(1L, 1L, "testUsername", "https://s3.url/image.jpg",
                "Test description", image.getUploadedAt(), 5, false);
        when(mapper.toDto(image, "testUsername", "https://s3.url/image.jpg", false))
                .thenReturn(expectedDto);

        ImageDto result = imageService.save(uploadRequestDto);

        assertThat(result).isEqualTo(expectedDto);
        verify(s3Service).uploadToS3(multipartFile, "users/1/test-s3-key");
        verify(s3Service).getSignedUrlForImage("users/1/test-s3-key");
        verify(userServiceClient).fetchUser(1L);
        verify(imageRepository).save(image);
    }

    @Test
    void returnImageDto_When_ImageExists() {
        when(imageRepository.findByIdExceptionally(1L))
                .thenReturn(image);
        when(s3Service.getSignedUrlForImage("users/1/test-s3-key"))
                .thenReturn("https://s3.url/image.jpg");
        when(userServiceClient.fetchUser(image.getUserId()))
                .thenReturn(Optional.of(new UserDto(1L, "testUsername", "testEmail")));
        when(likeRepository.existsByImageIdAndUserId(1L, 1L))
                .thenReturn(true);
        ImageDto expectedDto = new ImageDto(1L, 1L, "testUsername", "https://s3.url/image.jpg",
                "Test description", image.getUploadedAt(), 5, true);
        when(mapper.toDto(image, "testUsername", "https://s3.url/image.jpg", true))
                .thenReturn(expectedDto);

        ImageDto result = imageService.getById(1L);

        assertThat(result).isEqualTo(expectedDto);
        verify(imageRepository).findByIdExceptionally(1L);
        verify(s3Service).getSignedUrlForImage("users/1/test-s3-key");
        verify(userServiceClient).fetchUser(1L);
    }

    @Test
    void throwImageNotFoundException_When_ImageDoesNotExist() {
        when(imageRepository.findByIdExceptionally(100000L))
                .thenThrow(new ImageNotFoundException(100000L));

        assertThatThrownBy(() -> imageService.getById(100000L))
                .isInstanceOf(ImageNotFoundException.class)
                .hasMessageContaining("Image with id: (100000) not found");
    }

    @Test
    void throwUserNotFoundException_When_UserDoesNotExist() {
        when(imageRepository.findByIdExceptionally(1L))
                .thenReturn(image);
        when(s3Service.getSignedUrlForImage("users/1/test-s3-key"))
                .thenReturn("https://s3.url/image.jpg");
        when(userServiceClient.fetchUser(image.getUserId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->imageService.getById(1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with id: %d not found".formatted(image.getUserId()));

        verify(imageRepository).findByIdExceptionally(1L);
        verify(s3Service).getSignedUrlForImage("users/1/test-s3-key");
        verify(userServiceClient).fetchUser(1L);

    }

    @Test
    void returnWindowOfImages_When_GetAllWithValidScrollRequest() {
        KeySetScrollRequest scrollRequest = createScrollRequest(null);
        Window<Image> imageWindow = createImageWindow();

        when(imageRepository.findAllFilteredBy(
                scrollRequest.scrollPosition(),
                scrollRequest.sort(),
                scrollRequest.limit()))
                .thenReturn(imageWindow);

        when(likeRepository.findLikedImagesIdByUserFromSpecific(1L, List.of(1L, 2L)))
                .thenReturn(Set.of(1L));
        when(s3Service.getSignedUrlForImage(anyString()))
                .thenReturn("https://s3.url/image.jpg");
        when(userServiceClient.fetchUser(1L))
                .thenReturn(Optional.of(new UserDto(1L, "testUsername", "testEmail")));

        Window<ImageDto> result = imageService.getAll(scrollRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(imageRepository).findAllFilteredBy(
                scrollRequest.scrollPosition(),
                scrollRequest.sort(),
                scrollRequest.limit());
        verify(userServiceClient, times(2)).fetchUser(1L);
    }

    @Test
    void returnWindowOfImagesForTheLast10Days_When_GetAllWithValidScrollRequest() {
        Instant last10Days = Instant.now().minus(10, DAYS);
        KeySetScrollRequest scrollRequest = createScrollRequest(last10Days);
        Window<Image> imageWindow = createImageWindow();

        when(imageRepository.findAllFilteredByUploadedAtAfter(
                scrollRequest.tillDate(),
                scrollRequest.scrollPosition(),
                scrollRequest.sort(),
                scrollRequest.limit()))
                .thenReturn(imageWindow);

        when(likeRepository.findLikedImagesIdByUserFromSpecific(1L, List.of(1L, 2L)))
                .thenReturn(Set.of(1L));
        when(s3Service.getSignedUrlForImage(anyString()))
                .thenReturn("https://s3.url/image.jpg");
        when(userServiceClient.fetchUser(1L))
                .thenReturn(Optional.of(new UserDto(1L, "testUsername", "testEmail")));

        Window<ImageDto> result = imageService.getAll(scrollRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(imageRepository).findAllFilteredByUploadedAtAfter(
                scrollRequest.tillDate(),
                scrollRequest.scrollPosition(),
                scrollRequest.sort(),
                scrollRequest.limit());
        verify(userServiceClient, times(2)).fetchUser(1L);
    }

    @Test
    void returnWindowOfUserImagesForTheLast10Days_When_GetAllWithValidScrollRequest() {
        Instant last10Days = Instant.now().minus(10, DAYS);
        Long userId = 1L;
        KeySetScrollRequest scrollRequest = createScrollRequest(last10Days);
        Window<Image> imageWindow = createImageWindow();

        when(imageRepository.findAllFilteredByUserIdAndUploadedAtAfter(
                userId,
                scrollRequest.tillDate(),
                scrollRequest.scrollPosition(),
                scrollRequest.sort(),
                scrollRequest.limit()))
                .thenReturn(imageWindow);

        when(likeRepository.findLikedImagesIdByUserFromSpecific(1L, List.of(1L, 2L)))
                .thenReturn(Set.of(1L));
        when(s3Service.getSignedUrlForImage(anyString()))
                .thenReturn("https://s3.url/image.jpg");
        when(userServiceClient.fetchUser(1L))
                .thenReturn(Optional.of(new UserDto(1L, "testUsername", "testEmail")));

        Window<ImageDto> result = imageService.getUserImages(userId, scrollRequest);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(imageRepository).findAllFilteredByUserIdAndUploadedAtAfter(
                userId,
                scrollRequest.tillDate(),
                scrollRequest.scrollPosition(),
                scrollRequest.sort(),
                scrollRequest.limit());
        verify(userServiceClient, times(2)).fetchUser(1L);
    }

    @Test
    void updateImage_When_UserIsOwner() {
        when(imageRepository.findByIdAuthorized(1L, 1L))
                .thenReturn(image);
        when(likeRepository.existsByImageIdAndUserId(1L, 1L))
                .thenReturn(true);
        when(s3Service.getSignedUrlForImage("users/1/test-s3-key"))
                .thenReturn("https://s3.url/image.jpg");
        when(userServiceClient.fetchUser(1L))
                .thenReturn(Optional.of(new UserDto(1L, "testUsername", "testEmail")));
        String updatedDescription = "Updated description";
        ImageDto expectedDto = new ImageDto(1L, 1L, "testUsername", "https://s3.url/image.jpg",
                updatedDescription, image.getUploadedAt(), 5, true);
        when(mapper.toDto(image, "testUsername", "https://s3.url/image.jpg", true))
                .thenReturn(expectedDto);

        UpdateRequestDto updateRequest = new UpdateRequestDto(updatedDescription);
        ImageDto updatePost = imageService.updatePost(1L, updateRequest);

        assertThat(updatePost).isEqualTo(expectedDto);
        assertThat(updatePost.description()).isEqualTo(updatedDescription);
        verify(imageRepository).findByIdAuthorized(1L, 1L);
        verify(s3Service).getSignedUrlForImage("users/1/test-s3-key");
        verify(userServiceClient).fetchUser(1L);
    }

    @Test
    void throwNonAuthorizedAccessException_When_UserIsNotOwner() {
        UserPrincipal notOwner = new UserPrincipal(2L, "notOwner", "notOwner@example.com");
        HeaderAuthenticationToken authenticationToken = new HeaderAuthenticationToken(notOwner);
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(securityContext);

        when(imageRepository.findByIdAuthorized(1L, 2L))
                .thenThrow(new NonAuthorizedImageAccessException(2L, 1L));

        UpdateRequestDto updateRequest = new UpdateRequestDto("Updated description");

        assertThatThrownBy(() -> imageService.updatePost(1L, updateRequest))
                .isInstanceOf(NonAuthorizedImageAccessException.class);
    }

    @Test
    void deleteImage_When_UserIsOwner() {
        when(imageRepository.findByIdAuthorized(1L, 1L))
                .thenReturn(image);

        imageService.deletePost(1L);

        verify(imageRepository).deleteById(1L);
        verify(eventPublisher).publishEvent(any(ImageDeleted.class));
    }

    @Test
    void likeImage_When_LikeDoesNotExist() {
        when(likeRepository.findIdByImageIdAndUserId(1L, 1L))
                .thenReturn(Optional.empty());
        when(mapper.toModel(1L, 1L))
                .thenReturn(new Like());
        when(imageRepository.findByIdExceptionally(1L))
                .thenReturn(image);
        when(s3Service.getSignedUrlForImage("users/1/test-s3-key"))
                .thenReturn("https://s3.url/image.jpg");
        when(userServiceClient.fetchUser(image.getUserId()))
                .thenReturn(Optional.of(new UserDto(1L, "testUsername", "testEmail")));
        when(likeRepository.existsByImageIdAndUserId(1L, 1L))
                .thenReturn(true);
        ImageDto expectedDto = new ImageDto(1L, 1L, "testUsername", "https://s3.url/image.jpg",
                "Test description", image.getUploadedAt(), 5, true);
        when(mapper.toDto(image, "testUsername", "https://s3.url/image.jpg", true))
                .thenReturn(expectedDto);

        ImageDto result = imageService.likeImage(1L);

        verify(likeRepository).save(any(Like.class));
        verify(imageRepository).incrementLikes(1L);
        assertThat(result).isEqualTo(expectedDto);
        verify(imageRepository).findByIdExceptionally(1L);
        verify(s3Service).getSignedUrlForImage("users/1/test-s3-key");
        verify(userServiceClient).fetchUser(1L);
    }

    @Test
    void unlikeImage_When_LikeExists() {
        Like like = new Like();
        like.setId(1L);
        when(likeRepository.findIdByImageIdAndUserId(1L, 1L)).thenReturn(Optional.of(like));
        when(likeRepository.deleteLikeById(1L)).thenReturn(1);
        when(imageRepository.findByIdExceptionally(1L))
                .thenReturn(image);
        when(s3Service.getSignedUrlForImage("users/1/test-s3-key"))
                .thenReturn("https://s3.url/image.jpg");
        when(userServiceClient.fetchUser(image.getUserId()))
                .thenReturn(Optional.of(new UserDto(1L, "testUsername", "testEmail")));
        when(likeRepository.existsByImageIdAndUserId(1L, 1L))
                .thenReturn(false);
        ImageDto expectedDto = new ImageDto(1L, 1L, "testUsername", "https://s3.url/image.jpg",
                "Test description", image.getUploadedAt(), 5, false);
        when(mapper.toDto(image, "testUsername", "https://s3.url/image.jpg", false))
                .thenReturn(expectedDto);

        ImageDto result = imageService.likeImage(1L);

        verify(likeRepository).deleteLikeById(1L);
        verify(imageRepository).decrementLikes(1L);
        assertThat(result).isEqualTo(expectedDto);
        verify(imageRepository).findByIdExceptionally(1L);
        verify(s3Service).getSignedUrlForImage("users/1/test-s3-key");
        verify(userServiceClient).fetchUser(1L);
    }

    KeySetScrollRequest createScrollRequest(Instant tillDate) {
        Sort sort = Sort.by(Sort.Direction.DESC, "uploadedAt");
        Limit limit = Limit.of(10);
        KeysetScrollPosition scrollPosition = ScrollPosition.keyset();

        return KeySetScrollRequest.builder()
                .sort(sort)
                .limit(limit)
                .scrollPosition(scrollPosition)
                .tillDate(tillDate)
                .build();
    }

    Window<Image> createImageWindow() {
        Image image1 = createTestImage();
        Image image2 = createTestImage();
        image2.setId(2L);
        List<Image> content = List.of(image1, image2);
        return Window.from(content, _ -> ScrollPosition.keyset());
    }

}