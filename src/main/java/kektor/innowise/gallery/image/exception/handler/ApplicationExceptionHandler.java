package kektor.innowise.gallery.image.exception.handler;

import kektor.innowise.gallery.image.exception.ImageNotFoundException;
import kektor.innowise.gallery.image.exception.ImageUploadException;
import kektor.innowise.gallery.image.exception.InvalidCursorParameter;
import kektor.innowise.gallery.image.exception.NonAuthorizedImageAccessException;
import kektor.innowise.gallery.image.exception.UserNotFoundException;
import kektor.innowise.gallery.image.exception.UsernameNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Optional;


@Slf4j
@RestControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({
            ImageNotFoundException.class,
            UsernameNotFoundException.class,
            UserNotFoundException.class})
    public ErrorResponse handleImageNotFound(Exception ex) {
        return ErrorResponse.create(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NonAuthorizedImageAccessException.class)
    public ErrorResponse handleNonAuthorizedImageAccess(NonAuthorizedImageAccessException ex) {
        return ErrorResponse.create(ex, HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InvalidCursorParameter.class)
    ErrorResponse handleInvalidCursorParameter(InvalidCursorParameter ex) {
        return ErrorResponse.create(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ImageUploadException.class)
    ErrorResponse handleImageUploadFail(ImageUploadException ex) {
        log.error(ex.getMessage(), ex);
        return ErrorResponse.create(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<ProblemDetail> handleRestClientResponseException(RestClientResponseException ex) {
        ProblemDetail detail = ex.getResponseBodyAs(ProblemDetail.class);
        return ResponseEntity.status(ex.getStatusCode()).body(detail);
    }

    @ExceptionHandler(Exception.class)
    ErrorResponse handleAll(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ErrorResponse.create(ex, HttpStatus.INTERNAL_SERVER_ERROR, Optional.ofNullable(ex.getMessage())
                .orElse("Internal server error"));
    }
}
