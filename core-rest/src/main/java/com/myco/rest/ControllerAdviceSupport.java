package com.myco.rest;

import com.myco.util.v8n.V8NException;
import com.myco.util.values.ErrorMessage;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.modelling.command.ConflictingAggregateVersionException;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.util.WebUtils;

import java.util.NoSuchElementException;
import java.util.UUID;

public interface ControllerAdviceSupport {

  Logger logger();

  default String tagMessage(String tag, String msg) {
    return String.format("[%s] - %s", tag, msg);
  }

  @ExceptionHandler(value = {V8NException.class})
  default ResponseEntity<ErrorMessage> handleV8NException(V8NException ex, WebRequest request) {
    return handleExceptionInternal(ex, ex.getErrorMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(value = CommandExecutionException.class)
  default ResponseEntity<ErrorMessage> handleCommandExecutionException(
      CommandExecutionException ex, WebRequest request
  ) {
    String token = UUID.randomUUID().toString();
    logger().error(tagMessage(token, "COMMAND FAILURE; REQUEST: " + request), ex);
    return handleExceptionInternal(ex,
        new ErrorMessage.Builder().code(HttpStatus.BAD_REQUEST.name()).message(tagMessage(token, ex.getMessage()))
            .build(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  default ResponseEntity<ErrorMessage> doHandleBadRequest(Exception ex, WebRequest request) {
    String token = UUID.randomUUID().toString();
    logger().error(tagMessage(token, "BAD REQUEST: " + request), ex);
    return handleExceptionInternal(ex, new ErrorMessage.Builder().code(HttpStatus.BAD_REQUEST.name()).message(token)
            .detail(new ErrorMessage.Builder().code("exception").message(ex.getMessage()).build()).build(),
        new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(value = HttpMessageNotReadableException.class)
  default ResponseEntity<ErrorMessage> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, WebRequest request
  ) {
    return doHandleBadRequest(ex, request);
  }

  @ExceptionHandler(value = IllegalArgumentException.class)
  default ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
    return doHandleBadRequest(ex, request);
  }

  @ExceptionHandler(value = {IllegalStateException.class})
  default ResponseEntity<ErrorMessage> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
    return doHandleBadRequest(ex, request);
  }

  @ExceptionHandler(value = {ConflictingAggregateVersionException.class})
  default ResponseEntity<ErrorMessage> handleConflictingAggregateVersionException(
      ConflictingAggregateVersionException ex, WebRequest request
  ) {
    return doHandleBadRequest(ex, request);
  }

  @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class})
  default ResponseEntity<ErrorMessage> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex, WebRequest request
  ) {
    return doHandleBadRequest(ex, request);
  }

  @ExceptionHandler(value = {HttpClientErrorException.BadRequest.class})
  default ResponseEntity<ErrorMessage> handleHttpClientErrorExceptionBadRequest(
      HttpClientErrorException.BadRequest ex, WebRequest request
  ) {
    return doHandleBadRequest(ex, request);
  }

  default ResponseEntity<ErrorMessage> doNotFoundHandling(Exception ex, WebRequest request) {
    String token = UUID.randomUUID().toString();
    logger().debug(tagMessage(token, "NOT FOUND; REQUEST: " + request), ex);
    return handleExceptionInternal(ex,
        new ErrorMessage.Builder().code(HttpStatus.NOT_FOUND.name()).message(tagMessage(token, ex.getMessage()))
            .build(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler(value = {NoSuchElementException.class})
  default ResponseEntity<ErrorMessage> handleNoSuchElementException(NoSuchElementException ex, WebRequest request) {
    return doNotFoundHandling(ex, request);
  }

  @ExceptionHandler(value = {HttpClientErrorException.NotFound.class})
  default ResponseEntity<ErrorMessage> handleHttpClientErrorExceptionNotFound(
      HttpClientErrorException.NotFound ex, WebRequest request
  ) {
    return doNotFoundHandling(ex, request);
  }

  @ExceptionHandler(value = {HttpClientErrorException.Unauthorized.class})
  default ResponseEntity<ErrorMessage> handleHttpClientErrorExceptionUnauthorized(
      HttpClientErrorException.Unauthorized ex, WebRequest request
  ) {
    logger().error("NOT AUTHORIZED; REQUEST: " + request, ex);
    return handleExceptionInternal(ex, new ErrorMessage.Builder().code(HttpStatus.UNAUTHORIZED.name()).build(),
        new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
  }

  @ExceptionHandler(value = {HttpClientErrorException.Forbidden.class})
  default ResponseEntity<ErrorMessage> handleHttpClientErrorExceptionForbidden(
      HttpClientErrorException.Forbidden ex, WebRequest request
  ) {
    logger().error("FORBIDDEN; REQUEST: " + request, ex);
    return handleExceptionInternal(ex, new ErrorMessage.Builder().code(HttpStatus.FORBIDDEN.name()).build(),
        new HttpHeaders(), HttpStatus.FORBIDDEN, request);
  }

  @ExceptionHandler
  default ResponseEntity<ErrorMessage> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
    logger().error("ACCESS DENIED; REQUEST: " + request, ex);
    return handleExceptionInternal(ex, new ErrorMessage.Builder().code(HttpStatus.FORBIDDEN.name()).build(),
        new HttpHeaders(), HttpStatus.FORBIDDEN, request);
  }

  @ExceptionHandler(value = {MissingServletRequestParameterException.class})
  default ResponseEntity<ErrorMessage> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException ex, WebRequest request
  ) {
    String token = UUID.randomUUID().toString();
    logger().error(tagMessage(token, "BAD_REQUEST: " + request), ex);
    return handleExceptionInternal(ex,
        new ErrorMessage.Builder().code(HttpStatus.BAD_REQUEST.name()).message(tagMessage(token, ex.getMessage()))
            .build(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler
  default ResponseEntity<ErrorMessage> handleException(Exception ex, WebRequest request) {
    String token = UUID.randomUUID().toString();
    logger().error(tagMessage(token, "CATCH ALL HANDLER; REQUEST: " + request), ex);
    return new ResponseEntity<>(
        new ErrorMessage.Builder().code(HttpStatus.INTERNAL_SERVER_ERROR.name()).message(token).build(),
        new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * A single place to customize the response body of all Exception types.
   * <p>The default implementation sets the {@link WebUtils#ERROR_EXCEPTION_ATTRIBUTE}
   * request attribute and creates a {@link ResponseEntity} from the given
   * body, headers, and status.
   *
   * @param ex      the exception
   * @param body    the body for the response
   * @param headers the headers for the response
   * @param status  the response status
   * @param request the current request
   */
  default ResponseEntity<ErrorMessage> handleExceptionInternal(
      Exception ex, @Nullable ErrorMessage body, HttpHeaders headers, HttpStatus status, WebRequest request
  ) {

    if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
      request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
    }
    return new ResponseEntity<>(body, headers, status);
  }
}
