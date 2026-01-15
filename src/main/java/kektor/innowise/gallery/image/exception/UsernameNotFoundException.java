package kektor.innowise.gallery.image.exception;

public class UsernameNotFoundException extends RuntimeException {

    private static final String USER_NOT_FOUND = "User with username: (%s) not found";

    public UsernameNotFoundException(String username) {
        super(String.format(USER_NOT_FOUND, username));
    }
}
