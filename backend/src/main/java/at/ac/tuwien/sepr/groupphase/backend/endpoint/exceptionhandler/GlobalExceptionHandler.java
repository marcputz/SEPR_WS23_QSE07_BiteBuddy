package at.ac.tuwien.sepr.groupphase.backend.endpoint.exceptionhandler;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ErrorDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Register all your Java exceptions here to map them into meaningful HTTP exceptions.
 * If you have special cases which are only important for specific endpoints, use ResponseStatusExceptions
 * <a href="https://www.baeldung.com/exception-handling-for-rest-with-spring#responsestatusexception">Baeldung Responsestatusexception</a>
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Handles {@link NotFoundException}s occuring in REST endpoints.
     *
     * @param ex        the exception.
     * @param request   the request where the exception occurred.
     * @return          a ResponseEntity to send back to the client
     */
    @ExceptionHandler(value = {NotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());

        ErrorDto errorDto = new ErrorDto(HttpStatus.NOT_FOUND, ex);
        return super.handleExceptionInternal(ex, errorDto, new HttpHeaders(), errorDto.getStatus(), request);
    }

    /**
     * Handles {@link AuthenticationException}s occurring in REST endpoints.
     *
     * @param ex      the exception
     * @param request the request where the exception occurred
     * @return a ResponseEntity to send back to the client
     * @author Marc Putz
     */
    @ExceptionHandler(value = {AuthenticationException.class})
    protected ResponseEntity<Object> handleAuthenticationError(AuthenticationException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());

        ErrorDto errorDto = new ErrorDto(HttpStatus.UNAUTHORIZED, ex);
        return super.handleExceptionInternal(ex, errorDto, new HttpHeaders(), errorDto.getStatus(), request);
    }

    /**
     * Handles {@link DataStoreException}s occurring in REST endpoints.
     *
     * @param ex      the exception
     * @param request the request where the exception occurred
     * @return a ResponseEntity to send back to the client
     * @author Marc Putz
     */
    @ExceptionHandler(value = {DataStoreException.class})
    protected ResponseEntity<Object> handleDataStoreError(DataStoreException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());

        ErrorDto errorDto = new ErrorDto(HttpStatus.SERVICE_UNAVAILABLE, ex);
        return super.handleExceptionInternal(ex, errorDto, new HttpHeaders(), errorDto.getStatus(), request);
    }

    /**
     * Handles {@link ConflictException}s occurring in REST endpoints.
     *
     * @param ex      the exception
     * @param request the request where the exception occurred
     * @return a ResponseEntity to send back to the client
     */
    @ExceptionHandler(value = {ConflictException.class})
    protected ResponseEntity<Object> handleConflictError(ConflictException ex, WebRequest request) {
        LOGGER.warn("Conflict detected: {}", ex.getMessage());

        ErrorDto errorDto = new ErrorDto(HttpStatus.CONFLICT, ex);
        return super.handleExceptionInternal(ex, errorDto, new HttpHeaders(), errorDto.getStatus(), request);
    }

    /**
     * Handles {@link ValidationException}s occurring in REST endpoints.
     *
     * @param ex      the exception
     * @param request the request where the exception occurred
     * @return a ResponseEntity to send back to the client
     */
    @ExceptionHandler(value = {ValidationException.class})
    protected ResponseEntity<Object> handleValidationError(ValidationException ex, WebRequest request) {
        LOGGER.warn("Validation error: {}", ex.getMessage());

        ErrorDto errorDto = new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, ex);
        return super.handleExceptionInternal(ex, errorDto, new HttpHeaders(), errorDto.getStatus(), request);
    }

    /**
     * Handles {@link IllegalArgumentException}s occurring in REST endpoints.
     *
     * @param ex      the exception
     * @param request  the request where the exception occurred
     * @return a ResponseEntity to send back to the client
     */
    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected ResponseEntity<Object> handleIllegalArgument(RuntimeException ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());

        ErrorDto errorDto = new ErrorDto(HttpStatus.BAD_REQUEST, ex);
        return super.handleExceptionInternal(ex, errorDto, new HttpHeaders(), errorDto.getStatus(), request);
    }

}
