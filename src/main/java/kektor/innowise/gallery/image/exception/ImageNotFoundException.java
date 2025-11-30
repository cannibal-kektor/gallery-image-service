package kektor.innowise.gallery.image.exception;

public class ImageNotFoundException extends RuntimeException {

    private static final String IMAGE_NOT_FOUND = "Image with id: (%d) not found";

    public ImageNotFoundException(Long imageId) {
        super(String.format(IMAGE_NOT_FOUND, imageId));
    }
}
