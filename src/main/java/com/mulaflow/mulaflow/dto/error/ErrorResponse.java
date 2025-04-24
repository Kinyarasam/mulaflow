package com.mulaflow.mulaflow.dto.error;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String path;
    private HttpStatus status;
    private String error;
    private List<ValidationError> validationErrors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private String rejectedValue;
    }

    public ErrorResponse(HttpStatus status, String error, String message, String path) {
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.error = error;
    }
}
