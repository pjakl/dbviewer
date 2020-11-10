package com.pjakl.dbviewer.api;

import com.pjakl.dbviewer.api.views.ApiErrorView;
import com.pjakl.dbviewer.exception.DomainEntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.hibernate.exception.ConstraintViolationException;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataIntegrityViolationException.class)
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(CONFLICT)
    @ResponseBody
    public ApiErrorView handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        var errorViewBuilder = ApiErrorView.builder().message(e.getMessage());
        if (e.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException excp = (ConstraintViolationException) e.getCause();
            errorViewBuilder.message("Constraint violated"+ excp.getConstraintName());
            errorViewBuilder.detailedMessage(excp.getSQLException().getMessage());
        }

        return errorViewBuilder.build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public ApiErrorView handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return processBindingResult(e.getBindingResult());
    }

    @ExceptionHandler(DomainEntityNotFoundException.class)
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public ApiErrorView handleDomainEntityNotFound(DomainEntityNotFoundException e) {
        return ApiErrorView.builder().message(e.getMessage()).build();
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public ApiErrorView handleIllegalArgumentException(Exception e) {
        return ApiErrorView.builder().message(e.getMessage()).build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(METHOD_NOT_ALLOWED)
    @ResponseBody
    public ApiErrorView handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        StringBuilder builder = new StringBuilder();
        builder.append(e.getMethod());
        builder.append(" method is not supported for this request. Supported methods are ");
        e.getSupportedHttpMethods().forEach(t -> builder.append(t + " "));

        return ApiErrorView.builder().message(builder.toString()).build();
    }

    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public ApiErrorView handleJDBCConnectionError(CannotGetJdbcConnectionException e) {
        return ApiErrorView.builder().message(e.getMessage()).build();
    }

    // Generic catch-all error handler
    @ExceptionHandler(Exception.class)
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiErrorView handleAll(Exception e) {
        logger.error(String.format("Unhandled exception: %s", e.getMessage()), e);
        return ApiErrorView.builder().message("Internal error").detailedMessage(e.getMessage()).build();
    }

    private ApiErrorView processBindingResult(BindingResult bindingResult) {
        List<String> errors = bindingResult.getFieldErrors().stream()
                .map(err -> String.format("%s: %s", err.getField(), err.getDefaultMessage()))
                .sorted()
                .collect(Collectors.toList());

        List<String> globalErrors = bindingResult.getGlobalErrors().stream()
                .map(err -> String.format("%s: %s", err.getObjectName(), err.getDefaultMessage()))
                .sorted()
                .collect(Collectors.toList());

        errors.addAll(globalErrors);
        return ApiErrorView.builder().message("Invalid arguments").detailedMessage(Arrays.toString(errors.toArray())).build();
    }

}
