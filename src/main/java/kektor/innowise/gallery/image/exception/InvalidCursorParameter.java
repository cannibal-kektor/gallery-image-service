package kektor.innowise.gallery.image.exception;

public class InvalidCursorParameter extends RuntimeException {

    private static final String CURSOR_PARAMETER_INVALID = "Cursor parameter (%s=%s) is invalid";

    public InvalidCursorParameter(String cursorParameter, String cursorValue) {
        super(String.format(CURSOR_PARAMETER_INVALID, cursorParameter, cursorValue));
    }
}
