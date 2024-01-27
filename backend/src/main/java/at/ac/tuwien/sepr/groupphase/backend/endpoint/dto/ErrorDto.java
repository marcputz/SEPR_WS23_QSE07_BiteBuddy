package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

import java.util.Objects;

public class ErrorDto {

    @NotNull
    private int statusCode;

    @NotNull
    private String statusText;

    @NotNull
    private String statusDescription;

    private String reason;

    public ErrorDto(HttpStatus status, Throwable throwable) {
        this(status, throwable.getMessage());
    }

    public ErrorDto(HttpStatus status) {
        this(status, (String) null);
    }

    public ErrorDto(HttpStatus status, String reason) {
        this(status.value(), status.name(), status.getReasonPhrase(), reason);
    }

    public ErrorDto(int statusCode, String statusText, String statusDescription) {
        this(statusCode, statusText, statusDescription, null);
    }

    public ErrorDto(int statusCode, String statusText, String statusDescription, String reason) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.statusDescription = statusDescription;
        this.reason = reason;
    }

    public HttpStatus getStatus() {
        return HttpStatus.valueOf(this.statusCode);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ErrorDto errorDto = (ErrorDto) o;
        return statusCode == errorDto.statusCode && Objects.equals(statusText, errorDto.statusText) && Objects.equals(statusDescription, errorDto.statusDescription) && Objects.equals(reason, errorDto.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, statusText, statusDescription, reason);
    }
}
