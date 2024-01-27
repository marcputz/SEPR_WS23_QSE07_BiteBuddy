
package at.ac.tuwien.sepr.groupphase.backend.exception;

/**
 * Exception to be thrown by user-related classes when failing to retrieve user data.
 *
 * @author Marc Putz
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