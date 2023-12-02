package at.ac.tuwien.sepr.groupphase.backend.service.exception;

/**
 * Exception to be thrown by user-related classes when failing to retrieve user data.
 */
public class UserNotFoundException extends Exception {
    public UserNotFoundException() {
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Exception e) {
        super(e);
    }
}
