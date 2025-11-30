package kektor.innowise.gallery.image.service;

import io.awspring.cloud.s3.S3Template;
import kektor.innowise.gallery.image.exception.ImageUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class S3ImageServiceTest {

    @Mock
    S3Template s3Template;
    @InjectMocks
    S3ImageService s3ImageService;

    MultipartFile multipartFile;
    final String s3Key = "users/1/test-s3-key.jpeg";

    @BeforeEach
    void setUp() {
        multipartFile = new MockMultipartFile("image", "test.jpeg", "image/jpeg",
                "test binary bytes".getBytes());
    }

    @Test
    void uploadToS3Successfully_When_ValidFileProvided() {
        s3ImageService.uploadToS3(multipartFile, s3Key);
        verify(s3Template).upload(eq("images"), eq(s3Key), any(), any());
    }

    @Test
    void throwImageUploadException_When_IOExceptionOccurs() throws IOException {
        multipartFile = mock(MultipartFile.class);
        when(multipartFile.getInputStream()).thenThrow(new IOException("IO Exception"));
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpeg");

        assertThatThrownBy(() -> s3ImageService.uploadToS3(multipartFile, s3Key))
                .isInstanceOf(ImageUploadException.class)
                .hasMessageContaining("Image upload failed (test.jpeg)");
    }

    @Test
    void returnSignedUrl_When_ValidS3KeyProvided() throws MalformedURLException {
        String uri = "https://s3-signed-url.com/image.jpeg";
        URL url = URI.create(uri).toURL();
        when(s3Template.createSignedGetURL(eq("images"), eq(s3Key), any()))
                .thenReturn(url);

        String result = s3ImageService.getSignedUrlForImage(s3Key);

        assertThat(result).isEqualTo(uri);
        verify(s3Template).createSignedGetURL(eq("images"), eq(s3Key), any());
    }

    @Test
    void generateS3Key_When_ValidFileAndUserIdProvided() {
        Long userId = 1L;
        String result = s3ImageService.generateS3Key(multipartFile, userId);

        assertThat(result).startsWith("users/1/");
        assertThat(result).matches("users/1/[a-f0-9-]+\\.jpeg");
    }

}