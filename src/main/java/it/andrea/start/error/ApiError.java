package it.andrea.start.error;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import it.andrea.start.error.exception.ErrorCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(Instant timestamp, int status, String error, String code, String message, String path, List<String> details) {

    public ApiError(HttpStatus status, ErrorCode code, String message, String path) {
        this(Instant.now(), status.value(), status.getReasonPhrase(), code != null ? code.getCode() : null, message, path, null);
    }

    public ApiError(HttpStatus status, ErrorCode code, String message, String path, List<String> details) {
        this(Instant.now(), status.value(), status.getReasonPhrase(), code != null ? code.getCode() : null, message, path, details);
    }
}