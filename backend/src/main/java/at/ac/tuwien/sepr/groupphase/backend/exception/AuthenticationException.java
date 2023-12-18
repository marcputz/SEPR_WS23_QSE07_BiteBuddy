package at.ac.tuwien.sepr.groupphase.backend.exception;

/**
 * Exception class to describe authentication errors in the REST API.
 *
 * @author Marc Putz
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationException(Exception e) {
        super(e);
    }
}
