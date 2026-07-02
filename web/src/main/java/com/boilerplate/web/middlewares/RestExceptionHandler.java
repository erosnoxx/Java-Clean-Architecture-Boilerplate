package com.boilerplate.web.middlewares;

import com.boilerplate.domain.common.exceptions.ConflictException;
import com.boilerplate.domain.common.exceptions.DomainException;
import com.boilerplate.domain.common.exceptions.ExternalServiceException;
import com.boilerplate.domain.common.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Stream;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var errors = Stream.concat(
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(e -> new InvalidParam(e.getField(), e.getDefaultMessage())),
                ex.getBindingResult()
                        .getGlobalErrors()
                        .stream()
                        .map(e -> new InvalidParam(e.getObjectName(), e.getDefaultMessage()))
        ).toList();

        var pb = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pb.setTitle("validation failed");
        pb.setProperty("errors", errors);
        return pb;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        var pb = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pb.setTitle("malformed request body");
        pb.setDetail("the request body is missing or could not be parsed");
        return pb;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
        var pb = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pb.setTitle("missing required parameter");
        pb.setDetail("required parameter '" + ex.getParameterName() + "' is missing");
        return pb;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var pb = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pb.setTitle("invalid parameter type");
        pb.setDetail("parameter '" + ex.getName() + "' has an invalid value: '" + ex.getValue() + "'");
        return pb;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        var pb = ProblemDetail.forStatus(HttpStatus.METHOD_NOT_ALLOWED);
        pb.setTitle("method not allowed");
        pb.setDetail("method '" + ex.getMethod() + "' is not supported for this endpoint");
        return pb;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        var pb = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pb.setTitle("resource not found");
        pb.setDetail(ex.getMessage());
        return pb;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        log.warn("access denied — {}", ex.getMessage());
        var pb = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pb.setTitle("access denied");
        pb.setDetail(ex.getMessage());
        return pb;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        log.warn("authentication failed — {}", ex.getMessage());
        var pb = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pb.setTitle("authentication failed");
        pb.setDetail("invalid username or password");
        return pb;
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ProblemDetail handleExternalService(ExternalServiceException ex) {
        log.error("external service error — {}", ex.getMessage(), ex);
        var pb = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        pb.setTitle("external service error");
        pb.setDetail(ex.getMessage());
        return pb;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex) {
        log.error("unexpected error — {}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        var pb = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pb.setTitle("internal server error");
        pb.setDetail(ex.getMessage());
        return pb;
    }

    private record InvalidParam(String name, String reason) {}
}