package kektor.innowise.gallery.image.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {

    private static final String USER_NOT_FOUND = "User with id: %d not found";

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super(String.format(USER_NOT_FOUND, userId));
        this.userId = userId;
    }
}
