package kektor.innowise.gallery.image.exception;

public class NonAuthorizedImageAccessException extends RuntimeException {

    private static final String AUTHORIZATION_OWNER_ACCESS_EXCEPTION = "User: (id=%d) is not the owner of the accessed image(id=%d)";

    public NonAuthorizedImageAccessException(Long userId, Long imageId) {
        super(String.format(AUTHORIZATION_OWNER_ACCESS_EXCEPTION, userId, imageId));
    }
}