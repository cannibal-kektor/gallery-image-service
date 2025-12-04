package kektor.innowise.gallery.image.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import kektor.innowise.gallery.image.dto.UpdateRequestDto;
import kektor.innowise.gallery.image.dto.UserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.time.Instant;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static kektor.innowise.gallery.security.HeadersAuthenticationFilter.EMAIL_HEADER;
import static kektor.innowise.gallery.security.HeadersAuthenticationFilter.USER_ID_HEADER;
import static kektor.innowise.gallery.security.HeadersAuthenticationFilter.USERNAME_HEADER;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@EnableWireMock({
        @ConfigureWireMock(port = 8089)
})
@Sql(scripts = {
        "/sql/cleanup.sql",
        "/sql/test-data.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("test")
public class ImageServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:18-alpine"))
            .withDatabaseName("images_db")
            .withUsername("testUser")
            .withPassword("testPassword");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:4.0"))
            .withServices(LocalStackContainer.Service.S3)
            .withCopyToContainer(
                    MountableFile.forClasspathResource("aws/init-s3.sh", 0777),
                    "/etc/localstack/init/ready.d/init-s3.sh");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.s3.endpoint", localstack::getEndpoint);
        registry.add("spring.cloud.aws.credentials.access-key", localstack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localstack::getSecretKey);
        registry.add("spring.cloud.aws.region.static", localstack::getRegion);
        registry.add("gallery.security.protected-services.comment-service-url", () -> "http://localhost:8089");
        registry.add("gallery.security.protected-services.user-service-url", () -> "http://localhost:8089");
    }

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    HttpHeaders headers;
    UserDto user;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
        headers.add(USER_ID_HEADER, "1");
        headers.add(USERNAME_HEADER, "user1");
        headers.add(EMAIL_HEADER, "user1@test.com");

        user = new UserDto(1L, "testUsername", "testEmail");
    }

    @AfterEach
    void clear() {
        WireMock.resetToDefault();
    }

    @Test
    void returnImage_When_ImageExists() throws Exception {
        stubFor(WireMock.get(urlPathEqualTo("/api/users/id/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(user))
                        .withStatus(200)));

        mockMvc.perform(get("/api/images/{id}", 1L)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value(user.username()))
                .andExpect(jsonPath("$.description").value("Test description 1"));
    }

    @Test
    void returnNotFound_When_ImageDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/images/{id}", 10000L)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createImage_When_ValidUploadRequest() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/images/upload")
                        .file(imageFile)
                        .param("description", "New test image")
                        .headers(headers)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyString())));
    }

    @Test
    void returnBadRequest_When_InvalidUploadRequest() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile",
                "test-image.xml",
                MediaType.TEXT_XML_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/images/upload")
                        .file(imageFile)
                        .param("description", "     ")
                        .headers(headers)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateImage_When_UserIsOwner() throws Exception {
        String updatedDescription = "Updated description";
        UpdateRequestDto updateRequestDto = new UpdateRequestDto(updatedDescription);

        stubFor(WireMock.get(urlPathEqualTo("/api/users/id/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(user))
                        .withStatus(200)));

        mockMvc.perform(put("/api/images/{imageId}", 1L)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value(user.username()))
                .andExpect(jsonPath("$.description").value(updatedDescription));
    }

    @Test
    void returnForbidden_When_UserIsNotOwner() throws Exception {
        UpdateRequestDto updateRequestDto = new UpdateRequestDto("Updated description");

        mockMvc.perform(put("/api/images/{imageId}", 2L)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteImage_When_UserIsOwner() throws Exception {
        mockMvc.perform(delete("/api/images/{imageId}", 1L)
                        .headers(headers))
                .andExpect(status().isOk());
    }

    @Test
    void likeImage_When_ImageExists() throws Exception {
        mockMvc.perform(post("/api/images/{imageId}/like", 1L)
                        .headers(headers))
                .andExpect(status().isOk());

        stubFor(WireMock.get(urlPathEqualTo("/api/users/id/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(user))
                        .withStatus(200)));

        mockMvc.perform(get("/api/images/{id}", 1L)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likesCount").value(4));
    }

    @Test
    void returnPaginatedImages_When_ScrollParametersProvided() throws Exception {
        stubFor(WireMock.get(urlPathMatching("/api/users/id/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(user))
                        .withStatus(200)));

        mockMvc.perform(get("/api/images")
                        .headers(headers)
                        .param("size", "5")
                        .param("sort", "uploadedAt,desc")
                        .param("cursor-last-uploadedAt", Instant.now().toString())
                        .param("cursor-last-id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void returnUserImages_When_UserExists() throws Exception {
        stubFor(WireMock.get(urlPathMatching("/api/users/(username|id)/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(user))
                        .withStatus(200)));

        mockMvc.perform(get("/api/images/username/{username}", "testUsername")
                        .headers(headers)
                        .param("size", "10")
                        .param("sort", "uploadedAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void returnCurrentUserImages_When_Authenticated() throws Exception {
        stubFor(WireMock.get(urlPathMatching("/api/users/id/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(user))
                        .withStatus(200)));

        mockMvc.perform(get("/api/images/user/current")
                        .headers(headers)
                        .param("size", "10")
                        .param("sort", "uploadedAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void interServiceCallReturnImage_When_ImageExists() throws Exception {
        stubFor(WireMock.get(urlPathEqualTo("/api/users/id/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(user))
                        .withStatus(200)));

        headers.add("X-System-Internal-Call", "testOriginService");

        mockMvc.perform(get("/api/images/{id}/internal", 1L)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value(user.username()))
                .andExpect(jsonPath("$.description").value("Test description 1"));
    }
}