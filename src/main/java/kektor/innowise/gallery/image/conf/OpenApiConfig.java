package kektor.innowise.gallery.image.conf;


import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;

import java.math.BigDecimal;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Image Service API - Image Gallery Microservices",
                version = "${app.version}",
                description = """
                        Image Management Microservice for the Image Gallery application.
                        This service handles image upload, storage, retrieval.
                        Provides comprehensive image functionality with AWS S3 integration
                        and advanced pagination.
                        
                        ## Features
                        - Image upload with validation (JPEG, PNG)
                        - Advanced keyset pagination for efficient scrolling
                        - Image liking system
                        - User-specific image galleries
                        - Secure S3 storage with signed URLs
                        """,
                contact = @Contact(
                        name = "cannibal-kektor",
                        url = "https://github.com/cannibal-kektor"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        security = {
                @SecurityRequirement(name = OpenApiConfig.JWT_BEARER_TOKEN),
                @SecurityRequirement(name = OpenApiConfig.INTERNAL_SERVICE_AUTH)
        }

)
@SecurityScheme(
        name = OpenApiConfig.JWT_BEARER_TOKEN,
        description = """
                JWT authentication token obtained from the Authentication Service.
                The API Gateway validates the token and forwards user information in headers:
                - X-User-Id: User identifier
                - X-User-Email: User email address
                - X-User-Username: User username
                """,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@SecurityScheme(
        name = OpenApiConfig.INTERNAL_SERVICE_AUTH,
        description = """
                Internal service authentication for inter-service communication.
                Requires the X-System-Internal-Call header with the origin service name.
                Used for service-to-service calls within the microservices ecosystem.
                """,
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-System-Internal-Call"
)
public class OpenApiConfig {

    public static final String INTERNAL_SERVICE_AUTH = "internal-service";
    public static final String JWT_BEARER_TOKEN = "bearer-token";

    public static final String PROBLEM_DETAIL = "ProblemDetail";
    public static final String PROBLEM_DETAIL_RESPONSE = "ProblemDetailResponse";
    public static final String WINDOW_SIZE = "windowSize";
    public static final String SORT_CRITERIA = "sortCriteria";
    public static final String TILL_DATE = "tillDate";
    public static final String CURSOR_UPLOADED_AT = "cursorUploadedAt";
    public static final String CURSOR_LIKES_COUNT = "cursorLikesCount";
    public static final String CURSOR_ID = "cursorId";

    @Bean
    public OpenApiCustomizer commonParametersOpenApiCustomizer() {
        return openApi -> openApi.getComponents()
                .addParameters(WINDOW_SIZE,
                        new Parameter()
                                .name("size")
                                .description("Number of items per window")
                                .in(ParameterIn.QUERY.toString())
                                .schema(new IntegerSchema()
                                        .minimum(BigDecimal.valueOf(1))
                                        .maximum(BigDecimal.valueOf(100))))
                .addParameters(SORT_CRITERIA,
                        new Parameter()
                                .name("sort")
                                .description("Sorting criteria")
                                .in(ParameterIn.QUERY.toString())
                                .schema(new StringSchema().example("uploadedAt,desc")))
                .addParameters(TILL_DATE,
                        new Parameter()
                                .name("tillDate")
                                .description("Filter images uploaded after this date (ISO format)")
                                .in(ParameterIn.QUERY.toString())
                                .schema(new DateTimeSchema().example("2025-11-01T10:30:00Z")))
                .addParameters(CURSOR_UPLOADED_AT,
                        new Parameter()
                                .name("cursor-last-uploadedAt")
                                .description("Cursor for keyset pagination - last uploadedAt value")
                                .in(ParameterIn.QUERY.toString())
                                .schema(new DateTimeSchema().example("2025-11-01T10:30:00Z"))
                )
                .addParameters(CURSOR_LIKES_COUNT,
                        new Parameter()
                                .name("cursor-last-likesCount")
                                .description("Cursor for keyset pagination - last likesCount value")
                                .in(ParameterIn.QUERY.toString())
                                .schema(new IntegerSchema().format("int64").minimum(BigDecimal.valueOf(0))))
                .addParameters(CURSOR_ID,
                        new Parameter()
                                .name("cursor-last-id")
                                .description("Cursor for keyset pagination - last image ID")
                                .in(ParameterIn.QUERY.toString())
                                .schema(new IntegerSchema().format("int64")));
    }


    @Bean
    public OpenApiCustomizer commonErrorResponseOpenApiCustomizer() {
        MediaType mediaType = new MediaType().schema(new Schema<>().$ref(PROBLEM_DETAIL));
        Content content = new Content().addMediaType("application/problem+json", mediaType);

        //register ProblemDetail schema
        Schema<?> problemDetailSchema = ModelConverters.getInstance()
                .read(ProblemDetail.class)
                .get(PROBLEM_DETAIL);

        return openApi -> openApi.getComponents()
                .addSchemas(PROBLEM_DETAIL, problemDetailSchema)
                .addResponses(PROBLEM_DETAIL_RESPONSE,
                        new ApiResponse()
                                .description("RFC 7807 Error response")
                                .content(content)
                );
    }

}
