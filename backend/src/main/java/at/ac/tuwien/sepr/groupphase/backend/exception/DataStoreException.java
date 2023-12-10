package at.ac.tuwien.sepr.groupphase.backend.exception;

public class DataStoreException extends RuntimeException {

    public DataStoreException(String message) {
        super(message);
    }

    public DataStoreException(String message, Throwable cause) {
        super(message, cause);
    }

}
