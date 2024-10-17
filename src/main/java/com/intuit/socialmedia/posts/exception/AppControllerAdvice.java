package com.intuit.socialmedia.posts.exception;

import jakarta.xml.bind.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class AppControllerAdvice {

    @ExceptionHandler(value = { ValidationException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleValidationException(ValidationException ex, WebRequest request) {
        log.error("Validation exception" + ex.getMessage());
        return new ErrorMessage(HttpStatus.BAD_REQUEST, "Validation exception" + ex.getMessage(), request.getDescription(false));
    }

    @ExceptionHandler(value = { BindException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleBindException(BindException ex, WebRequest request) {
        final BindingResult result = ex.getBindingResult();
        String error = result.getAllErrors().stream().map(e -> {
            if (e instanceof FieldError) {
                return ((FieldError) e).getField() + " : " + e.getDefaultMessage();
            } else {
                return e.getObjectName() + " : " + e.getDefaultMessage();
            }
        }).collect(Collectors.joining(", "));
        log.error("400 Status Code:{}", error);
        return new ErrorMessage(HttpStatus.BAD_REQUEST, error, request.getDescription(false));
    }

    @ExceptionHandler(value = { ResourceNotFoundException.class })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ErrorMessage handleException(ResourceNotFoundException ex, WebRequest request) {
        log.error(ex.getMessage());
        return new ErrorMessage(HttpStatus.NO_CONTENT, ex.getMessage(), request.getDescription(false));
    }

    @ExceptionHandler(value = { BadCredentialsException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorMessage handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.error(ex.getMessage());
        return new ErrorMessage(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getDescription(false));
    }

    @ExceptionHandler(value = { IllegalArgumentException.class })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleIllegalArgumentExceptionException(Exception ex, WebRequest request) {
        log.error(ex.getMessage());
        return new ErrorMessage(HttpStatus.CONFLICT, ex.getMessage(), request.getDescription(false));
    }

    @ExceptionHandler(value = { Exception.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleException(Exception ex, WebRequest request) {
        log.error("Error in handling request", ex);
        return new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getDescription(false));
    }

}
