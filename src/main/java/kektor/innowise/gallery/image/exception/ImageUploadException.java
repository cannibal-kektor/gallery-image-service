package kektor.innowise.gallery.image.exception;

public class ImageUploadException extends RuntimeException {

    private static final String IMAGE_UPLOAD_FAIL = "Image upload failed (%s)";

    public ImageUploadException(String fileName) {
        super(String.format(IMAGE_UPLOAD_FAIL, fileName));
    }
}
