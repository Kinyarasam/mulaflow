package com.mulaflow.mulaflow.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.mulaflow.mulaflow.dto.error.ErrorResponse;
import com.mulaflow.mulaflow.exception.AuthenticationException;
import com.mulaflow.mulaflow.exception.BusinessRuleException;
import com.mulaflow.mulaflow.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ExceptionController {
    
    /**
     * Handle authentication failures
     *
     * @param ex The authentication exception
     * @param request The HTTP request
     * @return Error response with HTTP 401 status
     */
    @ExceptionHandler({
        AuthenticationException.class,
        org.springframework.security.core.AuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
        AuthenticationException ex, HttpServletRequest request
    ) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }

    /**
     * Handle authorization failure
     *
     * @param ex The authorization exception
     * @param request The HTTP request
     * @return Error response with HTTP 403 status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
        AccessDeniedException ex, HttpServletRequest request
    ) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, request);
    }

    /**
     * Handle resource not found scenarios
     *
     * @param ex The not found exception
     * @param request The HTTP request
     * @return Error response with HTTP 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
        ResourceNotFoundException ex, HttpServletRequest request
    ) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Handles business rule violations.
     * 
     * @param ex The business rule exception
     * @param request The HTTP request
     * @return error response with HTTP 422 status
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleException(
        BusinessRuleException ex, HttpServletRequest request
    ) {
        return buildErrorResponse(ex, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    /**
     * Handle invalid input data
     *
     * @param ex The method argument not valid exception
     * @param request The web request
     * @return Error response with 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex, WebRequest request
    ) {
        List<ErrorResponse.ValidationError> errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.ValidationError(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : null))
                .collect(Collectors.toList());

        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation error",
            "field_validation_failed",
            request.getDescription(false).replace("uri=", "")
        );
        response.setValidationErrors(errorMessage);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Fallback handler for all other exceptions
     *
     * @param ex The exception
     * @param request The HTTP request
     * @return Error response with HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
        Exception ex, HttpServletRequest request
    ) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Builds a standardized error response
     * 
     * @param ex The Exception
     * @param status The HTTP status
     * @param request The HTTP request
     * @return ResponseEntity containing the error response
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
        Exception ex, HttpStatus status, HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
            status,
            status.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String message = ex.getParameterName() + " parameter is missing";
        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Missing parameter",
            message,
            ""
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(HttpMessageNotReadableException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid request body",
            "Required request body is missing or invalid",
            ""
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
