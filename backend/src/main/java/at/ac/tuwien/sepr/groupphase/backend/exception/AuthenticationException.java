package at.ac.tuwien.sepr.groupphase.backend.exception;

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
